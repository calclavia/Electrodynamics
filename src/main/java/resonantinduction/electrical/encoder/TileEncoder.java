package resonantinduction.electrical.encoder;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import resonantinduction.core.ResonantInduction;
import resonantinduction.electrical.armbot.Program;
import resonantinduction.electrical.armbot.task.TaskRotateTo;
import resonantinduction.electrical.encoder.coding.IProgram;
import resonantinduction.electrical.encoder.coding.ITask;
import resonantinduction.electrical.encoder.coding.TaskRegistry;
import universalelectricity.api.vector.Vector2;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.TileExternalInventory;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;

public class TileEncoder extends TileExternalInventory implements ISidedInventory, IPacketReceiver
{
	private ItemStack disk;
	private IInventoryWatcher watcher;
	public static final int PROGRAM_PACKET_ID = 0;
	public static final int PROGRAM_CHANGE_PACKET_ID = 1;
	public static final int REMOVE_TASK_PACKET_ID = 2;
	public static final int NEW_TASK_PACKET_ID = 3;

	protected IProgram program;

	@Override
	public void initiate()
	{
		super.initiate();
		if (!this.worldObj.isRemote)
		{
			program = new Program();
			program.setTaskAt(0, 0, new TaskRotateTo());
		}
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		if (watcher != null)
		{
			watcher.inventoryChanged();
		}
	}

	@Override
	public String getInvName()
	{
		return "Encoder";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	public void setWatcher(IInventoryWatcher watcher)
	{
		this.watcher = watcher;
	}

	public IInventoryWatcher getWatcher()
	{
		return this.watcher;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (this.disk != null)
		{
			NBTTagCompound diskNBT = new NBTTagCompound();
			this.disk.writeToNBT(diskNBT);
			nbt.setCompoundTag("disk", diskNBT);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		NBTTagCompound diskNBT = nbt.getCompoundTag("disk");

		if (diskNBT != null)
		{
			this.disk = ItemStack.loadItemStackFromNBT(diskNBT);
		}

	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			int readInt = data.readInt();

			if (readInt == PROGRAM_PACKET_ID)
			{
				if (data.readBoolean())
				{
					Program program = new Program();
					program.load(PacketHandler.readNBTTagCompound(data));
					this.program = program;
				}
				else
				{
					this.program = null;
				}
			}
			else if (readInt == PROGRAM_CHANGE_PACKET_ID)
			{
				ITask task = TaskRegistry.getCommand(data.readUTF());
				task.setPosition(data.readInt(), data.readInt());
				task.load(PacketHandler.readNBTTagCompound(data));
				this.getProgram().setTaskAt(task.getCol(), task.getRow(), task);
				this.sendGUIPacket();
			}
			else if (readInt == NEW_TASK_PACKET_ID)
			{
				ITask task = TaskRegistry.getCommand(data.readUTF());
				task.setPosition(data.readInt(), data.readInt());
				task.load(PacketHandler.readNBTTagCompound(data));
				this.getProgram().insertTask(task.getCol(), task.getRow(), task);
				this.sendGUIPacket();
			}
			else if (readInt == REMOVE_TASK_PACKET_ID)
			{
				this.getProgram().setTaskAt(data.readInt(), data.readInt(), null);
				this.sendGUIPacket();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void sendGUIPacket()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		boolean exists = this.program != null;
		if (exists)
		{
			this.program.save(tag);
		}
		return ResonantInduction.PACKET_TILE.getPacket(this, TileEncoder.PROGRAM_PACKET_ID, this, exists, tag);

	}

	public void removeTask(Vector2 vec)
	{
		if (vec != null)
		{
			if (this.worldObj.isRemote)
			{
				PacketDispatcher.sendPacketToServer(ResonantInduction.PACKET_TILE.getPacket(this, TileEncoder.REMOVE_TASK_PACKET_ID, this, vec.intX(), vec.intY()));
			}
			else
			{
				this.program.setTaskAt(vec.intX(), vec.intY(), null);
			}
		}
	}

	public void updateTask(ITask editTask)
	{

		if (editTask != null)
		{
			if (this.worldObj.isRemote)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				editTask.save(nbt);
				PacketDispatcher.sendPacketToServer(ResonantInduction.PACKET_TILE.getPacket(this, PROGRAM_CHANGE_PACKET_ID, this, editTask.getMethodName(), editTask.getCol(), editTask.getRow(), nbt));
			}
			else
			{
				this.program.setTaskAt(editTask.getCol(), editTask.getRow(), editTask);
			}
		}

	}

	public void insertTask(ITask editTask)
	{
		if (editTask != null)
		{
			if (this.worldObj.isRemote)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				editTask.save(nbt);
				PacketDispatcher.sendPacketToServer(ResonantInduction.PACKET_TILE.getPacket(this, NEW_TASK_PACKET_ID, this, editTask.getMethodName(), editTask.getCol(), editTask.getRow(), nbt));
			}
			else
			{
				this.program.insertTask(editTask.getCol(), editTask.getRow(), editTask);
			}
		}

	}

	public IProgram getProgram()
	{
		if (this.program == null)
		{
			this.program = new Program();
		}
		return this.program;
	}
}

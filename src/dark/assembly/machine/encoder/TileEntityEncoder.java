package dark.assembly.machine.encoder;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import universalelectricity.core.vector.Vector2;
import universalelectricity.prefab.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import dark.api.al.coding.IProcessTask;
import dark.api.al.coding.IProgram;
import dark.api.al.coding.ITask;
import dark.api.al.coding.TaskRegistry;
import dark.assembly.armbot.Program;
import dark.assembly.armbot.command.TaskDrop;
import dark.assembly.armbot.command.TaskGOTO;
import dark.assembly.armbot.command.TaskGive;
import dark.assembly.armbot.command.TaskGrabItem;
import dark.assembly.armbot.command.TaskIF;
import dark.assembly.armbot.command.TaskRotateTo;
import dark.core.common.DarkMain;
import dark.core.network.PacketHandler;
import dark.core.prefab.machine.TileEntityMachine;
import dark.core.prefab.machine.TileEntityMachine.SimplePacketTypes;

public class TileEntityEncoder extends TileEntityMachine implements ISidedInventory
{
    private ItemStack disk;
    private IInventoryWatcher watcher;
    public static final String PROGRAM_PACKET_ID = "program", PROGRAM_CHANGE_PACKET_ID = "programChange", REMOVE_TASK_PACKET_ID = "removeTask", NEW_TASK_PACKET_ID = "newTask";
    protected IProgram program;

    public TileEntityEncoder()
    {
        super();
        this.hasGUI = true;
    }

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
    public boolean simplePacket(String id, ByteArrayDataInput dis, Player player)
    {
        try
        {
            boolean su = super.simplePacket(id, dis, player);
            if (!su)
            {
                if (this.worldObj.isRemote)
                {
                    if (id.equalsIgnoreCase(TileEntityEncoder.PROGRAM_PACKET_ID))
                    {

                        if (dis.readBoolean())
                        {
                            Program program = new Program();
                            program.load(PacketManager.readNBTTagCompound(dis));
                            this.program = program;
                        }
                        else
                        {
                            this.program = null;
                        }
                        return true;
                    }
                }
                else
                {
                    if (id.equalsIgnoreCase(TileEntityEncoder.PROGRAM_CHANGE_PACKET_ID))
                    {
                        ITask task = TaskRegistry.getCommand(dis.readUTF());
                        task.setPosition(dis.readInt(), dis.readInt());
                        task.load(PacketHandler.instance().readNBTTagCompound(dis));
                        this.getProgram().setTaskAt(task.getCol(), task.getRow(), task);
                        this.sendGUIPacket();
                        return true;
                    }
                    else if (id.equalsIgnoreCase(TileEntityEncoder.NEW_TASK_PACKET_ID))
                    {
                        System.out.println("New task packet");
                        ITask task = TaskRegistry.getCommand(dis.readUTF());
                        task.setPosition(dis.readInt(), dis.readInt());
                        task.load(PacketHandler.instance().readNBTTagCompound(dis));
                        this.getProgram().insertTask(task.getCol(), task.getRow(), task);
                        this.sendGUIPacket();
                        return true;
                    }
                    else if (id.equalsIgnoreCase(TileEntityEncoder.REMOVE_TASK_PACKET_ID))
                    {
                        this.getProgram().setTaskAt(dis.readInt(), dis.readInt(), null);
                        this.sendGUIPacket();
                        return true;
                    }
                }
            }
            return su;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return true;
        }
    }

    /** Sends a gui packet only to the given player */
    @Override
    public void sendGUIPacket(EntityPlayer entity)
    {
        if (entity != null)
        {
            PacketDispatcher.sendPacketToPlayer(this.getDescriptionPacket(), (Player) entity);
        }
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
        return PacketHandler.instance().getTilePacket(DarkMain.CHANNEL, this, TileEntityEncoder.PROGRAM_PACKET_ID, exists, tag);

    }

    public void removeTask(Vector2 vec)
    {
        if (vec != null)
        {
            if (this.worldObj.isRemote)
            {
                PacketDispatcher.sendPacketToServer(PacketHandler.instance().getTilePacket(DarkMain.CHANNEL, this, TileEntityEncoder.REMOVE_TASK_PACKET_ID, vec.intX(), vec.intY()));
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
                PacketDispatcher.sendPacketToServer(PacketHandler.instance().getTilePacket(DarkMain.CHANNEL, this, TileEntityEncoder.PROGRAM_CHANGE_PACKET_ID, editTask.getMethodName(), editTask.getCol(), editTask.getRow(), nbt));
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
                System.out.println("New task request");
                NBTTagCompound nbt = new NBTTagCompound();
                editTask.save(nbt);
                PacketDispatcher.sendPacketToServer(PacketHandler.instance().getTilePacket(DarkMain.CHANNEL, this, TileEntityEncoder.NEW_TASK_PACKET_ID, editTask.getMethodName(), editTask.getCol(), editTask.getRow(), nbt));
            }
            else
            {
                this.program.insertTask(editTask.getCol(), editTask.getRow(), editTask);
            }
        }

    }

    @Override
    public boolean isInvNameLocalized()
    {
        //TODO ?
        return false;
    }

    public IProgram getProgram()
    {
        return this.program;
    }

    @Override
    public Class<? extends Container> getContainer()
    {
        return ContainerEncoder.class;
    }

}

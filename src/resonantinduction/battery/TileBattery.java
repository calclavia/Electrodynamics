/**
 * 
 */
package resonantinduction.battery;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.api.ICapacitor;
import resonantinduction.base.ListUtil;
import universalelectricity.api.item.IEnergyItem;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;
import calclavia.lib.tile.TileEntityElectrical;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * A modular battery with no GUI.
 * 
 * @author Calclavia, AidanBrady
 */
public class TileBattery extends TileEntityElectrical implements IPacketSender, IPacketReceiver
{
	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public SynchronizedBatteryData structure = SynchronizedBatteryData.getBase(this);
	public SynchronizedBatteryData prevStructure;

	public float clientEnergy;
	public int clientCells;
	public float clientMaxEnergy;

	private EnumSet inputSides = EnumSet.allOf(ForgeDirection.class);

	public TileBattery()
	{

	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.ticks == 5 && !this.structure.isMultiblock)
			{
				this.update();
			}

			if (this.prevStructure != this.structure)
			{
				for (EntityPlayer player : playersUsing)
				{
					player.closeScreen();
				}

				updateClient();
			}

			this.prevStructure = structure;

			this.structure.wroteNBT = false;
			this.structure.didTick = false;

			if (this.playersUsing.size() > 0)
			{
				updateClient();
			}

			for (EntityPlayer player : this.playersUsing)
			{
				PacketDispatcher.sendPacketToPlayer(ResonantInduction.PACKET_TILE.getPacket(this, this.getPacketData(0).toArray()), (Player) player);
			}

			this.produce();
		}
	}

	public float getTransferThreshhold()
	{
		return this.structure.getVolume() * 50;
	}

	public void updateClient()
	{
		PacketDispatcher.sendPacketToAllPlayers(ResonantInduction.PACKET_TILE.getPacket(this, getPacketData(0).toArray()));
	}

	public void updateAllClients()
	{
		for (Vector3 vec : structure.locations)
		{
			TileBattery battery = (TileBattery) vec.getTileEntity(worldObj);
			PacketDispatcher.sendPacketToAllPlayers(ResonantInduction.PACKET_TILE.getPacket(battery, battery.getPacketData(0).toArray()));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (!structure.wroteNBT)
		{
			structure.wroteNBT = true;
		}
	}

	public void update()
	{
		if (!worldObj.isRemote && (structure == null || !structure.didTick))
		{
			new BatteryUpdateProtocol(this).updateBatteries();

			if (structure != null)
			{
				structure.didTick = true;
			}
		}
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player)
	{
		structure.isMultiblock = data.readBoolean();

		structure.height = data.readInt();
		structure.length = data.readInt();
		structure.width = data.readInt();
	}

	@Override
	public ArrayList getPacketData(int type)
	{
		ArrayList data = new ArrayList();
		data.add(structure.isMultiblock);

		data.add(structure.height);
		data.add(structure.length);
		data.add(structure.width);

		return data;
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return this.inputSides;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.complementOf(this.inputSides);
	}

	/**
	 * Toggles the input/output sides of the battery.
	 */
	public boolean toggleSide(ForgeDirection orientation)
	{
		if (this.inputSides.contains(orientation))
		{
			this.inputSides.remove(orientation);
			return false;
		}
		else
		{
			this.inputSides.add(orientation);
			return true;
		}
	}
}

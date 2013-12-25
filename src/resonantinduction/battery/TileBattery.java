/**
 * 
 */
package resonantinduction.battery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;
import calclavia.lib.prefab.tile.TileAdvanced;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * A modular battery.
 * 
 * @author Calclavia
 */
@UniversalClass
public class TileBattery extends TileAdvanced implements IPacketSender, IPacketReceiver, IEnergyInterface, IEnergyContainer
{
	public static final long STORAGE = 100000000;

	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public BatteryStructure structure = new BatteryStructure(this);

	public float clientEnergy;
	public int clientCells;
	public float clientMaxEnergy;

	@Override
	public void initiate()
	{
		this.updateStructure();
	}

	public void updateStructure()
	{
		if (!this.worldObj.isRemote)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = new Vector3(this).modifyPositionFromSide(dir).getTileEntity(this.worldObj);

				if (tile instanceof TileBattery)
				{
					this.structure.merge((TileBattery) tile);
				}
			}
		}
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{

			for (EntityPlayer player : this.playersUsing)
			{
				PacketDispatcher.sendPacketToPlayer(ResonantInduction.PACKET_TILE.getPacket(this, this.getPacketData(0).toArray()), (Player) player);
			}

			this.produce();
		}
	}

	private void produce()
	{

	}

	public float getTransferThreshhold()
	{
		return this.structure.getVolume() * 50;
	}

	public void updateClient()
	{
		PacketDispatcher.sendPacketToAllPlayers(ResonantInduction.PACKET_TILE.getPacket(this, getPacketData(0).toArray()));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.structure.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (!structure.wroteNBT)
		{
			this.structure.writeToNBT(nbt);
			structure.wroteNBT = true;
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
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public void setEnergy(ForgeDirection from, long energy)
	{
		this.structure.setEnergy(energy);
	}

	@Override
	public long getEnergy(ForgeDirection from)
	{
		return this.structure.getEnergy();
	}

	@Override
	public long getEnergyCapacity(ForgeDirection from)
	{
		return this.structure.getEnergyCapacity();
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		return this.structure.receiveEnergy(receive, doReceive);
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		return this.structure.extractEnergy(extract, doExtract);
	}
}

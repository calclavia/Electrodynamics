package resonantinduction.electrical.battery;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.electricity.IVoltageOutput;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyInterface;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;

import com.google.common.io.ByteArrayDataInput;

/**
 * A modular battery.
 * 
 * @author Calclavia
 */
public class TileBattery extends TileEnergyDistribution implements IVoltageInput, IVoltageOutput, IPacketSender, IPacketReceiver, IEnergyInterface, IEnergyContainer
{
	/**
	 * Tiers: 0, 1, 2
	 */
	public static final int MAX_TIER = 2;

	/** The transfer rate **/
	public static final long DEFAULT_WATTAGE = getEnergyForTier(0);

	/** Voltage increases as series connection increases */
	public static final long DEFAULT_VOLTAGE = UniversalElectricity.DEFAULT_VOLTAGE;

	public TileBattery()
	{
		this.energy = new EnergyStorageHandler(0);
		this.saveIOMap = true;
	}

	/**
	 * @param tier - 0, 1, 2
	 * @return
	 */
	public static long getEnergyForTier(int tier)
	{
		return (long) Math.pow(100000000, (tier / (MAX_TIER + 0.25f)) + 1);
	}

	@Override
	public void initiate()
	{
		super.initiate();
		energy.setCapacity(getEnergyForTier(getBlockMetadata()));
	}

	@Override
	public void updateEntity()
	{
		if (!this.worldObj.isRemote)
		{
			//energy.setMaxTransfer((long) Math.min(Math.pow(10000, this.getNetwork().getConnectors().size()), energy.getEnergyCapacity()));
			energy.setMaxTransfer(energy.getEnergyCapacity());
			markDistributionUpdate |= produce() > 0;
		}

		super.updateEntity();
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_TILE.getPacket(this, getPacketData(0).toArray());
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		energy.setEnergy(data.readLong());
		ioMap = data.readShort();
	}

	@Override
	public ArrayList getPacketData(int type)
	{
		ArrayList data = new ArrayList();
		data.add(energy.getEnergy());
		data.add(ioMap);
		return data;
	}

	@Override
	public long getVoltageOutput(ForgeDirection side)
	{
		return DEFAULT_VOLTAGE;
	}

	@Override
	public long getVoltageInput(ForgeDirection direction)
	{
		return DEFAULT_VOLTAGE;
	}

	@Override
	public void onWrongVoltage(ForgeDirection direction, long voltage)
	{

	}

	@Override
	public void setIO(ForgeDirection dir, int type)
	{
		super.setIO(dir, type);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}

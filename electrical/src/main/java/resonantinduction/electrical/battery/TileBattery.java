package resonantinduction.electrical.battery;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.electricity.IVoltageOutput;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.net.IConnector;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;
import calclavia.lib.prefab.tile.TileElectrical;

import com.google.common.io.ByteArrayDataInput;

/**
 * A modular battery.
 * 
 * @author Calclavia
 */
public class TileBattery extends TileElectrical implements IConnector<BatteryNetwork>, IVoltageInput, IVoltageOutput, IPacketSender, IPacketReceiver, IEnergyInterface, IEnergyContainer
{
	/**
	 * Tiers: 0, 1, 2
	 */
	public static final int MAX_TIER = 2;

	/** The transfer rate **/
	public static final long DEFAULT_WATTAGE = getEnergyForTier(0);

	/** Voltage increases as series connection increases */
	public static final long DEFAULT_VOLTAGE = UniversalElectricity.DEFAULT_VOLTAGE;

	private BatteryNetwork network;

	public boolean markClientUpdate = false;
	public boolean markDistributionUpdate = false;

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
		return (long) Math.pow(100000000, ((float) tier / (float) (MAX_TIER + 0.25f)) + 1);
	}

	@Override
	public void initiate()
	{
		this.updateStructure();
		energy.setCapacity(getEnergyForTier(getBlockMetadata()));
	}

	public void updateStructure()
	{
		if (!this.worldObj.isRemote)
		{
			for (Object obj : getConnections())
			{
				if (obj instanceof TileBattery)
				{
					this.getNetwork().merge(((TileBattery) obj).getNetwork());
				}
			}

			markDistributionUpdate = true;
			markClientUpdate = true;
		}
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			energy.setMaxTransfer((long) Math.min(Math.pow(10000, this.getNetwork().getConnectors().size()), energy.getEnergyCapacity()));

			long produce = produce();

			if ((markDistributionUpdate || produce > 0) && ticks % 5 == 0)
			{
				getNetwork().redistribute();
				markDistributionUpdate = false;
			}

			if (markClientUpdate && ticks % 5 == 0)
			{
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		long returnValue = super.onReceiveEnergy(from, receive, doReceive);
		markDistributionUpdate = true;
		markClientUpdate = true;
		return returnValue;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		long returnValue = super.onExtractEnergy(from, extract, doExtract);
		markDistributionUpdate = true;
		markClientUpdate = true;
		return returnValue;
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
	public BatteryNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new BatteryNetwork();
			this.network.addConnector(this);
		}

		return this.network;
	}

	@Override
	public void setNetwork(BatteryNetwork structure)
	{
		this.network = structure;
	}

	@Override
	public Object[] getConnections()
	{
		Object[] connections = new Object[6];

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = new Vector3(this).translate(dir).getTileEntity(this.worldObj);

			if (tile instanceof TileBattery)
			{
				connections[dir.ordinal()] = tile;
			}
		}

		return connections;
	}

	@Override
	public void invalidate()
	{
		this.getNetwork().redistribute(this);
		this.getNetwork().split(this);
		super.invalidate();
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
	public IConnector<BatteryNetwork> getInstance(ForgeDirection from)
	{
		return this;
	}

	@Override
	public void setIO(ForgeDirection dir, int type)
	{
		super.setIO(dir, type);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}

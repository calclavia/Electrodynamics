package resonantinduction.electrical.battery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
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

import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * A modular battery.
 * 
 * @author Calclavia
 */
public class TileBattery extends TileElectrical implements IConnector<BatteryStructure>, IVoltageInput, IVoltageOutput, IPacketSender, IPacketReceiver, IEnergyInterface, IEnergyContainer
{
	/** The transfer rate **/
	public static final long DEFAULT_WATTAGE = (long) (getEnergyForTier(1) * 0.01);

	/** Voltage increases as series connection increases */
	public static final long DEFAULT_VOLTAGE = UniversalElectricity.DEFAULT_VOLTAGE;

	private BatteryStructure structure;

	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public float clientEnergy;
	public int clientCells;
	public float clientMaxEnergy;

	public TileBattery()
	{
		this.energy = new EnergyStorageHandler(getEnergyForTier(1));
		this.saveIOMap = true;
	}

	public static long getEnergyForTier(int tier)
	{
		return (long) Math.pow(1000000, tier);
	}

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
					this.getNetwork().merge(((TileBattery) tile).getNetwork());
				}
			}

			this.energy.setMaxTransfer(DEFAULT_WATTAGE * this.getNetwork().get().size());
			this.getNetwork().redistribute();
		}
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.produce() > 0)
			{
				this.getNetwork().redistribute();
			}
		}
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		long returnValue = super.onReceiveEnergy(from, receive, doReceive);
		this.getNetwork().redistribute();
		return returnValue;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		long returnValue = super.onExtractEnergy(from, extract, doExtract);
		this.getNetwork().redistribute();
		return returnValue;
	}

	public void updateClient()
	{
		PacketDispatcher.sendPacketToAllPlayers(ResonantInduction.PACKET_TILE.getPacket(this, getPacketData(0).toArray()));
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
	}

	@Override
	public ArrayList getPacketData(int type)
	{
		ArrayList data = new ArrayList();
		return data;
	}

	@Override
	public BatteryStructure getNetwork()
	{
		if (this.structure == null)
		{
			this.structure = new BatteryStructure();
			this.structure.add(this);
		}

		return this.structure;
	}

	@Override
	public void setNetwork(BatteryStructure structure)
	{
		this.structure = structure;
	}

	@Override
	public Object[] getConnections()
	{
		Object[] connections = new Object[6];

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = new Vector3(this).modifyPositionFromSide(dir).getTileEntity(this.worldObj);

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
}

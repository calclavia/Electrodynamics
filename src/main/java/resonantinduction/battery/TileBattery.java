package resonantinduction.battery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import universalelectricity.api.UniversalClass;
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
@UniversalClass
public class TileBattery extends TileElectrical implements IConnector<BatteryStructure>, IPacketSender, IPacketReceiver, IEnergyInterface, IEnergyContainer
{
	public static final long STORAGE = 100000000;

	private BatteryStructure structure;

	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public float clientEnergy;
	public int clientCells;
	public float clientMaxEnergy;

	public TileBattery()
	{
		this.energy = new EnergyStorageHandler(STORAGE);
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

		}
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.getNetwork().getFirstNode() == this)
			{
				this.getNetwork().redistribute();
			}

			this.produce();
		}
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
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
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
		this.getNetwork().split(this);
		super.invalidate();
	}
}

package resonantinduction.wire.part;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.base.PartAdvanced;
import resonantinduction.wire.IAdvancedConductor;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.EnergyNetworkLoader;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;

//@UniversalClass
public abstract class PartConductor extends PartAdvanced implements IAdvancedConductor
{
	private IEnergyNetwork network;

	protected Object[] cachedConnections = new Object[6];

	/**
	 * Universal Electricity conductor functions.
	 */
	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		return this.getNetwork().produce(receive);
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long request, boolean doExtract)
	{
		return 0;
	}

	@Override
	public IEnergyNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.setNetwork(EnergyNetworkLoader.getNewNetwork(this));
		}

		return this.network;
	}

	@Override
	public void setNetwork(IEnergyNetwork network)
	{
		this.network = network;
	}

	@Override
	public void preRemove()
	{
		if (!world().isRemote)
		{
			this.getNetwork().split(this);
		}

		super.preRemove();
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	@Override
	public Object[] getConnections()
	{
		return this.cachedConnections;
	}

	/**
	 * Can externally connect?
	 */
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		Vector3 connectPos = new Vector3(tile()).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(world());
		return CompatibilityModule.canConnect(connectTile, direction.getOpposite());
	}

	/**
	 * Recalculates all the netwirk connections
	 */
	protected void recalculateConnections()
	{
		this.cachedConnections = new Object[6];
		/**
		 * Calculate all external connections with this conductor.
		 */
		for (byte i = 0; i < 6; i++)
		{
			ForgeDirection side = ForgeDirection.getOrientation(i);

			if (this.canConnect(side))
			{
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), side);
				cachedConnections[i] = tileEntity;
			}
		}
	}

	public abstract boolean canConnect(Object obj);
}

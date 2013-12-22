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
import codechicken.multipart.TileMultipart;

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
	 * Shouldn't need to be overridden. Override connectionPrevented instead
	 */
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		Vector3 connectPos = new Vector3(tile()).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(world());

		if (connectTile instanceof IConductor)
		{
			return canConnect((IConductor) connectTile);
		}

		return false;
	}

	public abstract boolean canConnect(IConductor conductor);

	public abstract boolean isBlockedOnSide(ForgeDirection side);

}

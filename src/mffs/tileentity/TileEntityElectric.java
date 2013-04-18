package mffs.tileentity;

import java.util.EnumSet;

import mffs.base.TileEntityFortron;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.prefab.implement.IRotatable;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;

public abstract class TileEntityElectric extends TileEntityFortron implements IPowerReceptor, IConnector, IVoltage
{
	/**
	 * The amount of watts received this tick. This variable should be deducted when used.
	 */
	public double prevWatts, wattsReceived = 0;

	private IPowerProvider powerProvider;

	public TileEntityElectric()
	{
		if (PowerFramework.currentFramework != null)
		{
			if (this.powerProvider == null)
			{
				this.powerProvider = PowerFramework.currentFramework.createPowerProvider();
				this.powerProvider.configure(0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			}
		}
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		this.prevWatts = this.wattsReceived;

		/**
		 * ElectricityManager works on server side.
		 */
		if (!this.worldObj.isRemote)
		{
			/**
			 * If the machine is disabled, stop requesting electricity.
			 */
			if (!this.isDisabled())
			{
				ElectricityPack electricityPack = ElectricityNetworkHelper.consumeFromMultipleSides(this, this.getConsumingSides(), this.getRequest());
				this.onReceive(electricityPack);
			}
			else
			{
				ElectricityNetworkHelper.consumeFromMultipleSides(this, new ElectricityPack());
			}
		}
		if (this.powerProvider != null)
		{
			int requiredEnergy = (int) (this.getRequest().getWatts() * UniversalElectricity.TO_BC_RATIO);
			float energyReceived = this.powerProvider.useEnergy(requiredEnergy, requiredEnergy, true);
			this.onReceive(ElectricityPack.getFromWatts(UniversalElectricity.BC3_RATIO * energyReceived, this.getVoltage()));
		}
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		return ElectricityNetworkHelper.getDirections(this);
	}

	/**
	 * Returns the amount of energy being requested this tick. Return an empty ElectricityPack if no
	 * electricity is desired.
	 */
	public ElectricityPack getRequest()
	{
		return new ElectricityPack();
	}

	/**
	 * Called right after electricity is transmitted to the TileEntity. Override this if you wish to
	 * have another effect for a voltage overcharge.
	 * 
	 * @param electricityPack
	 */
	public void onReceive(ElectricityPack electricityPack)
	{
		/**
		 * Creates an explosion if the voltage is too high.
		 */
		if (UniversalElectricity.isVoltageSensitive)
		{
			if (electricityPack.voltage > this.getVoltage())
			{
				this.worldObj.createExplosion(null, this.xCoord, this.yCoord, this.zCoord, 1.5f, true);
				return;
			}
		}

		this.wattsReceived = Math.min(this.wattsReceived + electricityPack.getWatts(), this.getWattBuffer());
	}

	/**
	 * @return The amount of internal buffer that may be stored within this machine. This will make
	 * the machine run smoother as electricity might not always be consistent.
	 */
	public double getWattBuffer()
	{
		return this.getRequest().getWatts() * 2;
	}

	@Override
	public double getVoltage()
	{
		return 0;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (this instanceof IRotatable)
		{
			return direction == ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
		}

		return true;
	}

	public ForgeDirection getDirection(IBlockAccess world, int x, int y, int z)
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
	}

	public void setDirection(World world, int x, int y, int z, ForgeDirection facingDirection)
	{
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, facingDirection.ordinal(), 2);
	}

	@Override
	public void setPowerProvider(IPowerProvider provider)
	{
		this.powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider()
	{
		return this.powerProvider;
	}

	@Override
	public void doWork()
	{

	}

	@Override
	public int powerRequest(ForgeDirection from)
	{
		if (this.canConnect(from))
		{
			return (int) (this.getRequest().getWatts() * UniversalElectricity.TO_BC_RATIO);
		}

		return 0;
	}
}

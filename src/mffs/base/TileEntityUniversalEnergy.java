package mffs.base;

import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.tile.IEnergySource;

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import calclavia.lib.IUniversalEnergyTile;

public abstract class TileEntityUniversalEnergy extends TileEntityModuleAcceptor implements IEnergySource, IUniversalEnergyTile
{
	/**
	 * The amount of watts received this tick. This variable should be deducted when used.
	 */
	public double prevWatts, wattsReceived = 0;

	private IPowerProvider powerProvider;

	public TileEntityUniversalEnergy()
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
			float requiredEnergy = (float) (this.getRequest().getWatts() * UniversalElectricity.TO_BC_RATIO);
			float energyReceived = this.powerProvider.useEnergy(0, requiredEnergy, true);

			if (energyReceived > 0)
			{
				this.onReceive(ElectricityPack.getFromWatts((UniversalElectricity.BC3_RATIO * energyReceived) / this.getVoltage(), this.getVoltage()));
			}
		}
	}

	public ElectricityPack produce(double watts)
	{
		ElectricityPack pack = new ElectricityPack(watts / this.getVoltage(), this.getVoltage());
		ElectricityPack remaining = ElectricityNetworkHelper.produceFromMultipleSides(this, pack);

		/**
		 * Try outputting BuildCraft power.
		 */
		if (remaining.getWatts() > 0)
		{
			EnumSet<ForgeDirection> approachingDirections = ElectricityNetworkHelper.getDirections(this);

			for (ForgeDirection direction : approachingDirections)
			{
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), direction);

				if (this.getPowerProvider(tileEntity) != null)
				{
					this.getPowerProvider(tileEntity).receiveEnergy((float) (remaining.getWatts() * UniversalElectricity.TO_BC_RATIO), direction.getOpposite());
				}
			}
		}

		if (remaining.getWatts() > 0)
		{
			EnergyTileSourceEvent evt = new EnergyTileSourceEvent(this, (int) (remaining.getWatts() * UniversalElectricity.TO_IC2_RATIO));
			MinecraftForge.EVENT_BUS.post(evt);
			remaining = new ElectricityPack((evt.amount * UniversalElectricity.IC2_RATIO) / remaining.voltage, remaining.voltage);
		}

		return remaining;
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
		return 120;
	}

	/**
	 * IC2
	 */
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		if (this.getConsumingSides() != null)
		{
			return this.getConsumingSides().contains(direction.toForgeDirection());
		}
		else
		{
			return true;
		}
	}

	@Override
	public boolean isAddedToEnergyNet()
	{
		return this.ticks > 0;
	}

	@Override
	public int demandsEnergy()
	{
		return (int) Math.ceil(this.getRequest().getWatts() * UniversalElectricity.TO_IC2_RATIO);
	}

	@Override
	public int injectEnergy(Direction direction, int i)
	{
		double givenElectricity = i * UniversalElectricity.IC2_RATIO;
		double rejects = 0;

		if (givenElectricity > this.getWattBuffer())
		{
			rejects = givenElectricity - this.getRequest().getWatts();
		}

		this.onReceive(new ElectricityPack(givenElectricity / this.getVoltage(), this.getVoltage()));

		return (int) (rejects * UniversalElectricity.TO_IC2_RATIO);
	}

	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return this.canConnect(direction.toForgeDirection());
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return 2048;
	}

	/**
	 * Buildcraft
	 */
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
			return (int) Math.ceil(this.getRequest().getWatts() * UniversalElectricity.TO_BC_RATIO);
		}

		return 0;
	}

	public IPowerProvider getPowerProvider(TileEntity tileEntity)
	{
		if (tileEntity instanceof IPowerReceptor)
		{
			return ((IPowerReceptor) tileEntity).getPowerProvider();
		}

		return null;
	}
}

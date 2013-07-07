package mffs.base;

import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySource;

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.compatiblity.Compatiblity;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import universalelectricity.prefab.implement.IRotatable;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import calclavia.lib.IUniversalEnergyTile;
import cpw.mods.fml.common.Loader;

public abstract class TileEntityUniversalEnergy extends TileEntityModuleAcceptor implements IEnergySource, IUniversalEnergyTile
{
	public float energyStored;

	private PowerHandler powerHandler = new PowerHandler(this, Type.MACHINE);

	@Override
	public void initiate()
	{
		super.initiate();
		this.powerHandler.configure(0, Integer.MAX_VALUE, 0, (float) Math.ceil(this.getMaxEnergyStored() * Compatiblity.TO_BC_RATIO));
		MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
	}

	@Override
	public void invalidate()
	{
		MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		super.invalidate();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote && this.powerHandler != null)
		{
			float requiredEnergy = (float) (this.getRequest(null) * Compatiblity.TO_BC_RATIO);
			float energyReceived = this.powerHandler.useEnergy(0, requiredEnergy, true);
			this.receiveElectricity(ElectricityPack.getFromWatts(Compatiblity.BC3_RATIO * energyReceived, this.getVoltage()), true);
		}
	}

	public ElectricityPack produce(float watts)
	{
		ElectricityPack pack = ElectricityPack.getFromWatts(watts, this.getVoltage());
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

				if (tileEntity instanceof IPowerReceptor)
				{
					PowerReceiver receiver = ((IPowerReceptor) tileEntity).getPowerReceiver(direction.getOpposite());

					if (receiver != null)
					{
						receiver.receiveEnergy(Type.MACHINE, remaining.getWatts() * Compatiblity.TO_BC_RATIO, direction.getOpposite());
					}
				}
			}
		}

		if (Loader.isModLoaded("IC2") && remaining.getWatts() > 0)
		{
			EnergyTileSourceEvent evt = new EnergyTileSourceEvent(this, (int) (remaining.getWatts() * Compatiblity.TO_IC2_RATIO));
			MinecraftForge.EVENT_BUS.post(evt);
			remaining = new ElectricityPack((evt.amount * Compatiblity.IC2_RATIO) / remaining.voltage, remaining.voltage);
		}

		return remaining;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (this instanceof IRotatable)
		{
			return direction.ordinal() == this.getBlockMetadata();
		}

		return true;
	}

	@Override
	public float receiveElectricity(ElectricityPack electricityPack, boolean doReceive)
	{
		return 0;
	}

	@Override
	public float getRequest(ForgeDirection direction)
	{
		return 0;
	}

	@Override
	public float getVoltage()
	{
		return 120;
	}

	public void setEnergyStored(float energy)
	{
		this.energyStored = Math.max(Math.min(energy, this.getMaxEnergyStored()), 0);
	}

	public float getEnergyStored()
	{
		return this.energyStored;
	}

	public float getMaxEnergyStored()
	{
		return this.getRequest(ForgeDirection.UNKNOWN) * 2;
	}

	/**
	 * Buildcraft
	 */
	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		return this.powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{

	}

	/**
	 * IC2
	 */
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return this.canConnect(direction.toForgeDirection());
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return this.canConnect(direction.toForgeDirection());
	}

	@Override
	public boolean isAddedToEnergyNet()
	{
		return this.ticks > 0;
	}

	@Override
	public int demandsEnergy()
	{
		return (int) Math.ceil(this.getRequest(ForgeDirection.UNKNOWN) * Compatiblity.TO_IC2_RATIO);
	}

	@Override
	public int injectEnergy(Direction direction, int i)
	{
		float givenElectricity = i * Compatiblity.IC2_RATIO;
		float rejects = 0;

		if (givenElectricity > this.getMaxEnergyStored())
		{
			rejects = givenElectricity - this.getMaxEnergyStored();
		}

		this.receiveElectricity(ElectricityPack.getFromWatts(givenElectricity, this.getVoltage()), true);

		return (int) (rejects * Compatiblity.TO_IC2_RATIO);
	}

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return Integer.MAX_VALUE;
	}
}

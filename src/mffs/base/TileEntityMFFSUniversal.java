package mffs.base;

import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.compatiblity.Compatiblity;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import universalelectricity.prefab.tile.ElectricityHandler;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cpw.mods.fml.common.Loader;

public abstract class TileEntityMFFSUniversal extends TileEntityModuleAcceptor implements IElectrical, IElectricalStorage, IEnergySource, IPowerReceptor, IEnergySink
{
	public ElectricityHandler electricityHandler;

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
			float requiredEnergy = this.getRequest(null) * Compatiblity.TO_BC_RATIO;
			float energyReceived = this.powerHandler.useEnergy(0, requiredEnergy, true);
			this.electricityHandler.receiveElectricity(ElectricityPack.getFromWatts(Compatiblity.BC3_RATIO * energyReceived, this.getVoltage()), true);
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
	public float getRequest(ForgeDirection direction)
	{
		return 0;
	}

	@Override
	public float getProvide(ForgeDirection direction)
	{
		return 0;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public float getVoltage()
	{
		return 120;
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, direction.ordinal(), 3);
	}

	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
	{
		return this.electricityHandler.receiveElectricity(receive, doReceive);
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide)
	{
		return this.electricityHandler.provideElectricity(request, doProvide);
	}

	@Override
	public void setEnergyStored(float energy)
	{
		this.electricityHandler.setEnergyStored(energy);
	}

	@Override
	public float getEnergyStored()
	{
		return this.electricityHandler.getEnergyStored();
	}

	@Override
	public float getMaxEnergyStored()
	{
		return this.electricityHandler.getMaxEnergyStored();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.electricityHandler.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		this.electricityHandler.writeToNBT(nbt);
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
		float consumed = this.receiveElectricity(direction.toForgeDirection(), ElectricityPack.getFromWatts(givenElectricity, this.getVoltage()), true);
		float rejects = givenElectricity - consumed;

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

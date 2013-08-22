package mffs.base;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.electricity.ElectricityHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

/**
 * A universal electricity tile used for tiles that consume or produce electricity.
 * 
 * Extend this class or use as a reference for your own implementation of compatible electrical
 * tiles.
 * 
 * @author micdoodle8, Calclavia
 * 
 */
public abstract class TileEntityMFFSUniversal extends TileEntityModuleAcceptor implements IElectrical, IElectricalStorage, IEnergySink, IEnergySource, IPowerReceptor
{
	protected boolean isAddedToEnergyNet;
	public PowerHandler bcPowerHandler;
	public Type bcBlockType = Type.MACHINE;

	public float energyStored = 0;
	public float maxEnergyStored = 0;

	public TileEntityMFFSUniversal(float maxEnergy)
	{
		this(0, maxEnergy);
	}

	public TileEntityMFFSUniversal(float initialEnergy, float maxEnergy)
	{
		this.energyStored = initialEnergy;
		this.maxEnergyStored = maxEnergy;
		this.bcPowerHandler = new PowerHandler(this, this.bcBlockType);
		this.bcPowerHandler.configure(0, 100, 0, (int) Math.ceil(maxEnergy * Compatibility.BC3_RATIO));
	}

	/**
	 * Recharges electric item.
	 */
	public void recharge(ItemStack itemStack)
	{
		this.setEnergyStored(this.getEnergyStored() - ElectricItemHelper.chargeItem(itemStack, this.getProvide(ForgeDirection.UNKNOWN)));
	}

	/**
	 * Discharges electric item.
	 */
	public void discharge(ItemStack itemStack)
	{
		this.setEnergyStored(this.getEnergyStored() + ElectricItemHelper.dischargeItem(itemStack, this.getProvide(ForgeDirection.UNKNOWN)));
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		// Register to the IC2 Network
		if (!this.worldObj.isRemote && !this.isAddedToEnergyNet)
		{
			if (Compatibility.isIndustrialCraft2Loaded())
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			}

			this.isAddedToEnergyNet = true;
		}

		this.produce();
	}

	public void produce()
	{
		if (!this.worldObj.isRemote)
		{
			for (ForgeDirection outputDirection : this.getOutputDirections())
			{
				this.produceUE(outputDirection);
				this.produceBuildCraft(outputDirection);
			}
		}

		if (Compatibility.isBuildcraftLoaded())
		{
			/**
			 * Cheat BuildCraft powerHandler and always empty energy inside of it.
			 */
			this.receiveElectricity(this.bcPowerHandler.getEnergyStored(), true);
			this.bcPowerHandler.setEnergy(0);
		}
	}

	public void produceBuildCraft(ForgeDirection outputDirection)
	{
		if (!this.worldObj.isRemote && outputDirection != null && outputDirection != ForgeDirection.UNKNOWN)
		{
			float provide = this.getProvide(outputDirection);

			if (this.getEnergyStored() >= provide && provide > 0)
			{
				if (Compatibility.isBuildcraftLoaded())
				{
					TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(outputDirection).getTileEntity(this.worldObj);

					if (tileEntity instanceof IPowerReceptor)
					{
						PowerReceiver receiver = ((IPowerReceptor) tileEntity).getPowerReceiver(outputDirection.getOpposite());

						if (receiver != null)
						{
							float bc3Provide = provide * Compatibility.TO_BC_RATIO;
							float energyUsed = Math.min(receiver.receiveEnergy(this.bcBlockType, bc3Provide, outputDirection.getOpposite()), bc3Provide);
							this.setEnergyStored(this.getEnergyStored() - (bc3Provide - (energyUsed * Compatibility.TO_BC_RATIO)));
						}
					}
				}
			}
		}
	}

	public void produceUE(ForgeDirection outputDirection)
	{
		if (!this.worldObj.isRemote && outputDirection != null && outputDirection != ForgeDirection.UNKNOWN)
		{
			float provide = this.getProvide(outputDirection);

			if (provide > 0)
			{
				TileEntity outputTile = VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this), outputDirection);
				IElectricityNetwork outputNetwork = ElectricityHelper.getNetworkFromTileEntity(outputTile, outputDirection);

				if (outputNetwork != null)
				{
					ElectricityPack powerRequest = outputNetwork.getRequest(this);

					if (powerRequest.getWatts() > 0)
					{
						ElectricityPack sendPack = ElectricityPack.min(ElectricityPack.getFromWatts(this.getEnergyStored(), this.getVoltage()), ElectricityPack.getFromWatts(provide, this.getVoltage()));
						float rejectedPower = outputNetwork.produce(sendPack, this);
						this.setEnergyStored(this.getEnergyStored() - (sendPack.getWatts() - rejectedPower));
					}
				}
			}
		}
	}

	/**
	 * The electrical input direction.
	 * 
	 * @return The direction that electricity is entered into the tile. Return null for no input. By
	 * default you can accept power from all sides.
	 */
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}

	/**
	 * The electrical output direction.
	 * 
	 * @return The direction that electricity is output from the tile. Return null for no output. By
	 * default it will return an empty EnumSet.
	 */
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}

	/**
	 * IC2 Methods
	 */
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return this.canConnect(direction);
	}

	@Override
	public void invalidate()
	{
		this.unloadTileIC2();
		super.invalidate();
	}

	@Override
	public void onChunkUnload()
	{
		this.unloadTileIC2();
		super.onChunkUnload();
	}

	private void unloadTileIC2()
	{
		if (this.isAddedToEnergyNet && this.worldObj != null)
		{
			if (Compatibility.isIndustrialCraft2Loaded())
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			}

			this.isAddedToEnergyNet = false;
		}
	}

	@Override
	public double demandedEnergyUnits()
	{
		return Math.ceil(this.getRequest(ForgeDirection.UNKNOWN) * Compatibility.TO_IC2_RATIO);
	}

	@Override
	public double injectEnergyUnits(ForgeDirection direction, double amount)
	{
		if (this.getInputDirections().contains(direction))
		{
			float convertedEnergy = (float) (amount * Compatibility.IC2_RATIO);
			ElectricityPack toSend = ElectricityPack.getFromWatts(convertedEnergy, this.getVoltage());
			float receive = this.receiveElectricity(direction, toSend, true);

			// Return the difference, since injectEnergy returns left over energy, and
			// receiveElectricity returns energy used.
			return Math.round(amount - (receive * Compatibility.TO_IC2_RATIO));
		}

		return amount;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return receiver instanceof IEnergyTile && direction.equals(this.getOutputDirections());
	}

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public double getOfferedEnergy()
	{
		return this.getProvide(ForgeDirection.UNKNOWN) * Compatibility.TO_IC2_RATIO;
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.provideElectricity((float) amount * Compatibility.IC2_RATIO, true);
	}

	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
	{
		if (this.getInputDirections().contains(from))
		{
			return this.receiveElectricity(receive, doReceive);
		}

		return 0;
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide)
	{
		if (this.getOutputDirections().contains(from))
		{
			return this.provideElectricity(request, doProvide);
		}

		return new ElectricityPack();
	}

	/**
	 * A non-side specific version of receiveElectricity for you to optionally use it internally.
	 */
	public float receiveElectricity(ElectricityPack receive, boolean doReceive)
	{
		if (receive != null)
		{
			float prevEnergyStored = this.getEnergyStored();
			float newStoredEnergy = Math.min(this.getEnergyStored() + receive.getWatts(), this.getMaxEnergyStored());

			if (doReceive)
			{
				this.setEnergyStored(newStoredEnergy);
			}

			return Math.max(newStoredEnergy - prevEnergyStored, 0);
		}

		return 0;
	}

	public float receiveElectricity(float energy, boolean doReceive)
	{
		return this.receiveElectricity(ElectricityPack.getFromWatts(energy, this.getVoltage()), doReceive);
	}

	/**
	 * A non-side specific version of provideElectricity for you to optionally use it internally.
	 */
	public ElectricityPack provideElectricity(ElectricityPack request, boolean doProvide)
	{
		if (request != null)
		{
			float requestedEnergy = Math.min(request.getWatts(), this.energyStored);

			if (doProvide)
			{
				this.setEnergyStored(this.energyStored - requestedEnergy);
			}

			return ElectricityPack.getFromWatts(requestedEnergy, this.getVoltage());
		}

		return new ElectricityPack();
	}

	public ElectricityPack provideElectricity(float energy, boolean doProvide)
	{
		return this.provideElectricity(ElectricityPack.getFromWatts(energy, this.getVoltage()), doProvide);
	}

	@Override
	public void setEnergyStored(float energy)
	{
		this.energyStored = Math.max(Math.min(energy, this.getMaxEnergyStored()), 0);
	}

	@Override
	public float getEnergyStored()
	{
		return this.energyStored;
	}

	public void setMaxEnergyStored(float maxEnergyStored)
	{
		this.maxEnergyStored = maxEnergyStored;
	}

	@Override
	public float getMaxEnergyStored()
	{
		return this.maxEnergyStored;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (direction == null || direction.equals(ForgeDirection.UNKNOWN))
		{
			return false;
		}

		return this.getInputDirections().contains(direction) || this.getOutputDirections().contains(direction);
	}

	@Override
	public float getVoltage()
	{
		return 120;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.energyStored = nbt.getFloat("energyStored");
		this.maxEnergyStored = nbt.getFloat("maxEnergyStored");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setFloat("energyStored", this.energyStored);
		nbt.setFloat("maxEnergyStored", this.maxEnergyStored);
	}

	/**
	 * BuildCraft power support
	 */
	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		return this.bcPowerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{

	}

	@Override
	public World getWorld()
	{
		return this.getWorldObj();
	}
}

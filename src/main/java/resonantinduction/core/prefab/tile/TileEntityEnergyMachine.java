package resonantinduction.core.prefab.tile;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.old.lib.interfaces.IPowerLess;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.electricity.IVoltageOutput;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;

/**
 * Basic energy tile that can consume power
 * 
 * Based off both UE universal electrical tile, and electrical tile prefabs
 * 
 * @author DarkGuardsman
 */
public abstract class TileEntityEnergyMachine extends TileEntityMachine implements IEnergyInterface, IEnergyContainer, IPowerLess, IVoltageInput, IVoltageOutput
{
	/** Forge Ore Directory name of the item to toggle infinite power mode */
	public static String powerToggleItemID = "battery";
	/** Demand per tick in watts */
	protected long JOULES_PER_TICK;
	/** Max limit of the internal battery/buffer of the machine */
	protected long MAX_JOULES_STORED;
	/** Current energy stored in the machine's battery/buffer */
	protected long energyStored = 0;
	/** Should we run without power */
	private boolean runWithoutPower = true;
	/** Point by which this machines suffers low voltage damage */
	protected long brownOutVoltage = -1;
	/** Point by which this machines suffers over voltage damage */
	protected long shortOutVoltage = -1;
	/** Voltage by which the machine was designed and rated for */
	protected long ratedVoltage = 240;

	public TileEntityEnergyMachine()
	{
		this.brownOutVoltage = this.getVoltage() / 2;
		this.shortOutVoltage = (long) ((Math.sqrt(2) * this.getVoltage()) + 0.05 * this.getVoltage());
	}

	public TileEntityEnergyMachine(long wattsPerTick)
	{
		this();
		this.JOULES_PER_TICK = wattsPerTick;
		this.MAX_JOULES_STORED = wattsPerTick * 20;
	}

	public TileEntityEnergyMachine(long wattsPerTick, long maxEnergy)
	{
		this(wattsPerTick);
		this.MAX_JOULES_STORED = maxEnergy;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (!this.worldObj.isRemote && this.isFunctioning())
		{
			this.consumePower(this.JOULES_PER_TICK, true);
		}
	}

	/** Does this tile have power to run and do work */
	@Override
	public boolean canFunction()
	{
		return super.canFunction() && (this.runPowerLess() || this.consumePower(this.JOULES_PER_TICK, false));
	}

	/** Called when a player activates the tile's block */
	public boolean onPlayerActivated(EntityPlayer player)
	{
		if (player != null && player.capabilities.isCreativeMode)
		{
			ItemStack itemStack = player.getHeldItem();
			if (itemStack != null)
			{
				for (ItemStack stack : OreDictionary.getOres(powerToggleItemID))
				{
					if (stack.isItemEqual(itemStack))
					{
						this.togglePowerMode();
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		// TODO use this to reset any values that are based on the block as this gets called when
		// the block changes
	}

	/* ********************************************
	 * Electricity logic
	 * *********************************************
	 */

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		if (!this.runPowerLess() && this.getInputDirections().contains(from) && receive > 0)
		{
			long prevEnergyStored = Math.max(this.getEnergy(from), 0);
			long newStoredEnergy = Math.min(this.getEnergy(from) + receive, this.getEnergyCapacity(from));
			if (doReceive)
			{
				this.setEnergy(from, newStoredEnergy);
			}

			return Math.max(newStoredEnergy - prevEnergyStored, 0);
		}
		return 0;
	}

	/** Called to consume power from the internal storage */
	protected boolean consumePower(long watts, boolean doDrain)
	{
		if (watts <= 0)
		{
			return true;
		}
		if (!this.runPowerLess() && this.getEnergy(ForgeDirection.UNKNOWN) >= watts)
		{
			if (doDrain)
			{
				this.setEnergy(ForgeDirection.UNKNOWN, this.getEnergyStored() - watts);
			}
			return true;
		}
		return this.runPowerLess();
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long request, boolean doExtract)
	{
		if (this.getOutputDirections().contains(from) && request > 0)
		{
			long requestedEnergy = Math.min(request, this.energyStored);
			if (doExtract)
			{
				this.setEnergy(from, this.energyStored - requestedEnergy);
			}
			return requestedEnergy;
		}
		return 0;
	}

	/** Called to produce power using the output enumset for directions to output in */
	protected void produce()
	{
		for (ForgeDirection direction : this.getOutputDirections())
		{
			if (direction != ForgeDirection.UNKNOWN)
			{
				TileEntity entity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), direction);
				if (CompatibilityModule.canConnect(entity, direction.getOpposite()))
				{
					long output = this.onExtractEnergy(direction, this.JOULES_PER_TICK, false);
					long input = CompatibilityModule.receiveEnergy(entity, direction.getOpposite(), output, true);
					if (input > 0 && this.onExtractEnergy(direction, input, true) > 0)
					{
						break;
					}
				}
			}
		}
	}

	@Override
	public long getVoltageInput(ForgeDirection direction)
	{
		if (this.getInputDirections().contains(direction))
		{
			return this.ratedVoltage;
		}
		return 0;
	}

	@Override
	public void onWrongVoltage(ForgeDirection direction, long voltage)
	{
		if (voltage > this.ratedVoltage)
		{
			if (voltage > this.shortOutVoltage)
			{
				// TODO damage machine
			}
		}
		else
		{
			if (voltage < this.brownOutVoltage)
			{
				// TODO cause machine to run slow
			}
		}
	}

	@Override
	public long getVoltageOutput(ForgeDirection direction)
	{
		if (this.getOutputDirections().contains(direction))
		{
			return this.ratedVoltage;
		}
		return 0;
	}

	/* ********************************************
	 * Electricity connection logic
	 * *********************************************
	 */

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (direction == null || direction.equals(ForgeDirection.UNKNOWN))
		{
			return false;
		}

		return this.getInputDirections().contains(direction) || this.getOutputDirections().contains(direction);
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

	/* ********************************************
	 * Machine energy parms
	 * *********************************************
	 */

	public long getVoltage()
	{
		return this.ratedVoltage;
	}

	public TileEntityEnergyMachine setVoltage(long volts)
	{
		this.ratedVoltage = volts;
		return this;
	}

	public long getMaxEnergyStored()
	{
		return this.MAX_JOULES_STORED;
	}

	@Override
	public long getEnergyCapacity(ForgeDirection from)
	{
		if (this.canConnect(from) || from == ForgeDirection.UNKNOWN)
		{
			return this.getMaxEnergyStored();
		}
		return 0;
	}

	public void setMaxEnergyStored(long energy)
	{
		this.MAX_JOULES_STORED = energy;
	}

	public long getEnergyStored()
	{
		return this.energyStored;
	}

	@Override
	public void setEnergy(ForgeDirection from, long energy)
	{
		if (this.canConnect(from) || from == ForgeDirection.UNKNOWN)
		{
			this.energyStored = Math.max(Math.min(energy, this.getMaxEnergyStored()), 0);
		}
	}

	@Override
	public long getEnergy(ForgeDirection from)
	{
		if (this.canConnect(from) || from == ForgeDirection.UNKNOWN)
		{
			return this.energyStored;
		}
		return 0;
	}

	@Override
	public boolean runPowerLess()
	{
		return !runWithoutPower;
	}

	@Override
	public void setPowerLess(boolean bool)
	{
		runWithoutPower = !bool;
	}

	public void togglePowerMode()
	{
		this.setPowerLess(!this.runPowerLess());
	}

	public long getJoulesPerTick()
	{
		return this.JOULES_PER_TICK;
	}

	public long getJoulesPerSec()
	{
		return getJoulesPerTick() * 20;
	}

	public long getJoulesPerMin()
	{
		return getJoulesPerSec() * 60;
	}

	public long getJoulesPerHour()
	{
		return getJoulesPerMin() * 60;
	}

	public TileEntityEnergyMachine setJoulesPerTick(long energy)
	{
		this.JOULES_PER_TICK = energy;
		return this;
	}

	public TileEntityEnergyMachine setJoulesPerSecound(long energy)
	{
		this.JOULES_PER_TICK = energy / 20;
		return this;
	}

	public TileEntityEnergyMachine setJoulesPerMin(long energy)
	{
		this.JOULES_PER_TICK = energy / 1200;
		return this;
	}

	public TileEntityEnergyMachine setJoulesPerHour(long energy)
	{
		this.JOULES_PER_TICK = energy / 72000;
		return this;
	}

	/* ********************************************
	 * DATA/SAVE/LOAD
	 * *********************************************
	 */

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		NBTBase tag = nbt.getTag("energyStored");
		if (tag instanceof NBTTagFloat)
		{
			this.energyStored = (long) nbt.getFloat("energyStored") * 1000;
		}
		else if (tag instanceof NBTTagLong)
		{
			this.energyStored = nbt.getLong("energyStored");
		}

		runWithoutPower = !nbt.getBoolean("shouldPower");
		this.functioning = nbt.getBoolean("isRunning");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("shouldPower", !runWithoutPower);
		nbt.setLong("energyStored", this.getEnergyStored());
		nbt.setBoolean("isRunning", this.functioning);
	}

}

package com.builtbroken.minecraft.prefab;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.electricity.ElectricityHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

import com.builtbroken.minecraft.interfaces.IPowerLess;

/** Basic energy tile that can consume power
 * 
 * Based off both UE universal electrical tile, and electrical tile prefabs
 * 
 * @author DarkGuardsman */
public abstract class TileEntityEnergyMachine extends TileEntityMachine implements IElectrical, IElectricalStorage, IPowerLess
{
    /** Forge Ore Directory name of the item to toggle infinite power mode */
    public static String powerToggleItemID = "battery";
    /** Demand per tick in watts */
    protected float JOULES_PER_TICK;
    /** Max limit of the internal battery/buffer of the machine */
    protected float MAX_JOULES_STORED;
    /** Current energy stored in the machine's battery/buffer */
    protected float energyStored = 0;
    /** Should we run without power */
    private boolean runWithoutPower = true;
    /** Point by which this machines suffers low voltage damage */
    protected float brownOutVoltage = -1;
    /** Point by which this machines suffers over voltage damage */
    protected float shortOutVoltage = -1;
    /** Voltage by which the machine was designed and rated for */
    protected float ratedVoltage = 240;

    public TileEntityEnergyMachine()
    {
        this.brownOutVoltage = this.getVoltage() / 2;
        this.shortOutVoltage = (float) ((Math.sqrt(2) * this.getVoltage()) + 0.05 * this.getVoltage());
    }

    public TileEntityEnergyMachine(float wattsPerTick)
    {
        this();
        this.JOULES_PER_TICK = wattsPerTick;
        this.MAX_JOULES_STORED = wattsPerTick * 20;
    }

    public TileEntityEnergyMachine(float wattsPerTick, float maxEnergy)
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

    /* ********************************************
     * Electricity reception logic
     ***********************************************/

    @Override
    public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
    {
        if (!this.runPowerLess() && receive != null && this.canConnect(from))
        {
            if (receive != null && receive.voltage > this.shortOutVoltage)
            {
                if (doReceive)
                {
                    this.onDisable(20 + this.worldObj.rand.nextInt(100));
                }
                return 0;
            }
            return this.receiveElectricity(receive.getWatts(), doReceive);

        }
        return 0;
    }

    /** A non-side specific version of receiveElectricity for you to optionally use it internally. */
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

    /* ********************************************
     * Electricity transmition logic
     ***********************************************/

    /** Called to consume power from the internal storage */
    public boolean consumePower(float watts, boolean doDrain)
    {
        if (watts <= 0)
        {
            return true;
        }
        if (!this.runPowerLess() && this.getEnergyStored() >= watts)
        {
            if (doDrain)
            {
                this.setEnergyStored(this.getEnergyStored() - watts);
            }
            return true;
        }
        return this.runPowerLess();
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

    /** A non-side specific version of provideElectricity for you to optionally use it internally. */
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

    /** Produces energy on all sides */
    public void produceAllSides()
    {
        if (!this.worldObj.isRemote)
        {
            for (ForgeDirection outputDirection : this.getOutputDirections())
            {
                if (this.getOutputDirections().contains(outputDirection))
                {
                    this.produceDirection(outputDirection);
                }
            }
        }
    }

    /** Produces energy only on the given side */
    public void produceDirection(ForgeDirection outputDirection)
    {
        //TODO detect machines and power them if they are directly next to this machine
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
                        this.provideElectricity(sendPack.getWatts() - rejectedPower, true);
                    }
                }
            }
        }
    }

    /* ********************************************
     * Electricity connection logic
     ***********************************************/

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        if (direction == null || direction.equals(ForgeDirection.UNKNOWN))
        {
            return false;
        }

        return this.getInputDirections().contains(direction) || this.getOutputDirections().contains(direction);
    }

    /** The electrical input direction.
     * 
     * @return The direction that electricity is entered into the tile. Return null for no input. By
     * default you can accept power from all sides. */
    public EnumSet<ForgeDirection> getInputDirections()
    {
        return EnumSet.allOf(ForgeDirection.class);
    }

    /** The electrical output direction.
     * 
     * @return The direction that electricity is output from the tile. Return null for no output. By
     * default it will return an empty EnumSet. */
    public EnumSet<ForgeDirection> getOutputDirections()
    {
        return EnumSet.noneOf(ForgeDirection.class);
    }

    /* ********************************************
     * Machine energy parms
     ***********************************************/

    @Override
    public float getVoltage()
    {
        return this.ratedVoltage;
    }

    public TileEntityEnergyMachine setVoltage(float volts)
    {
        this.ratedVoltage = volts;
        return this;
    }

    @Override
    public float getMaxEnergyStored()
    {
        return this.MAX_JOULES_STORED;
    }

    public void setMaxEnergyStored(float energy)
    {
        this.MAX_JOULES_STORED = energy;
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

    @Override
    public float getRequest(ForgeDirection direction)
    {
        return Math.max(this.getMaxEnergyStored() - this.getEnergyStored(), 0);
    }

    @Override
    public float getProvide(ForgeDirection direction)
    {
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

    public TileEntityEnergyMachine setJoulesPerTick(float energy)
    {
        this.JOULES_PER_TICK = energy;
        return this;
    }

    public TileEntityEnergyMachine setJoulesPerSecound(float energy)
    {
        this.JOULES_PER_TICK = energy / 20;
        return this;
    }

    public TileEntityEnergyMachine setJoulesPerHour(float energy)
    {
        this.JOULES_PER_TICK = energy / 1200;
        return this;
    }

    /* ********************************************
     * DATA/SAVE/LOAD
     ***********************************************/

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.energyStored = nbt.getFloat("energyStored");
        runWithoutPower = !nbt.getBoolean("shouldPower");
        this.functioning = nbt.getBoolean("isRunning");

        if (nbt.hasKey("wattsReceived"))
        {
            this.energyStored = (float) nbt.getDouble("wattsReceived");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setBoolean("shouldPower", !runWithoutPower);
        nbt.setFloat("energyStored", this.energyStored);
        nbt.setBoolean("isRunning", this.functioning);
    }

}

package resonantinduction.electrical.charger;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyInterface;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.prefab.tile.TileExternalInventory;
import calclavia.lib.utility.inventory.ExternalInventory;

/** @author Darkguardsman */
public class TileCharger extends TileExternalInventory implements IRotatable, IEnergyInterface, IEnergyContainer
{
    private long energyCap = 0;
    private long energyStored = 0;
    private ChargerMode currentMode = ChargerMode.SINGLE;

    private static enum ChargerMode
    {
        SINGLE(1),
        DUAL(2),
        MULTI(4);
        public final int limit;

        private ChargerMode(int limit)
        {
            this.limit = limit;
        }
    }

    @Override
    public void initiate()
    {
        super.initiate();
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (this.ticks % 5 == 0)
        {
            this.energyCap = 0;
            this.energyStored = 0;
            for (int i = 0; i < this.getSizeInventory(); i++)
            {
                this.energyCap += CompatibilityModule.getMaxEnergyItem(this.getStackInSlot(i));
                this.energyStored += CompatibilityModule.getEnergyItem(this.getStackInSlot(i));
            }
        }
    }

    @Override
    public ForgeDirection getDirection()
    {
        return ForgeDirection.getOrientation(this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
    }

    @Override
    public void setDirection(ForgeDirection direction)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        return direction == this.getDirection().getOpposite();
    }

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        long energyUsed = 0;
        long energyLeft = receive;
        for (int i = 0; i < this.getSizeInventory(); i++)
        {
            long input = CompatibilityModule.chargeItem(this.getStackInSlot(i), energyLeft, true);
            energyUsed += input;
            energyLeft -= input;
            if (energyLeft <= 0)
                break;
        }
        return energyUsed;
    }

    @Override
    public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
    {
        return 0;
    }

    @Override
    public void setEnergy(ForgeDirection from, long energy)
    {
    }

    @Override
    public long getEnergy(ForgeDirection from)
    {
        if (this.canConnect(from))
        {
            return this.energyStored;
        }
        return 0;
    }

    @Override
    public long getEnergyCapacity(ForgeDirection from)
    {
        if (this.canConnect(from))
        {
            return this.energyCap;
        }
        return 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        this.currentMode = ChargerMode.values()[nbt.getInteger("chargerMode")];
        this.inventory = new ExternalInventory(this, this.currentMode.limit);
        super.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("chargerMode", this.currentMode.ordinal());
    }

}

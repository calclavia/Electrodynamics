package resonantinduction.assemblyline.machine;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;

import com.builtbroken.minecraft.prefab.TileEntityEnergyMachine;

public abstract class TileEntityGenerator extends TileEntityEnergyMachine
{
    public TileEntityGenerator()
    {
        super();
    }

    public TileEntityGenerator(long wattsPerTick)
    {
        super(wattsPerTick);
    }

    public TileEntityGenerator(long wattsPerTick, long maxEnergy)
    {
        super(wattsPerTick, maxEnergy);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (!this.worldObj.isRemote && this.enabled)
        {
            this.consumeFuel();
            if (this.isFunctioning())
            {
                this.produce();
            }
        }
    }

    @Override
    protected void produce()
    {
        for (ForgeDirection direction : this.getOutputDirections())
        {
            if (direction != ForgeDirection.UNKNOWN)
            {
                TileEntity entity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), direction);
                if (CompatibilityModule.canConnect(entity, direction.getOpposite()))
                {
                    CompatibilityModule.receiveEnergy(entity, direction.getOpposite(), this.JOULES_PER_TICK, true);
                }
            }
        }
    }

    @Override
    public long onExtractEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        if (this.canConnect(from) && this.getOutputDirections().contains(from))
        {
            return Math.min(receive, this.JOULES_PER_TICK);
        }
        return 0;
    }

    @Override
    public boolean canFunction()
    {
        return this.enabled && this.hasFuel();
    }

    /** Does this generator have fuel time left to burn */
    public abstract boolean hasFuel();

    /** Called each tick to handle anything fuel related */
    public abstract void consumeFuel();

    /* ********************************************
     * Electricity reception logic -  all of which is set to zero to prevent input from wires
     ***********************************************/

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        return 0;
    }

    @Override
    public long getEnergy(ForgeDirection from)
    {
        return 0;
    }

    @Override
    public long getEnergyCapacity(ForgeDirection from)
    {
        return 0;
    }
}

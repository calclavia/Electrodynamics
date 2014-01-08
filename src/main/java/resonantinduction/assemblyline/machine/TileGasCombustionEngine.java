package resonantinduction.assemblyline.machine;

import resonantinduction.assemblyline.fluid.EnumGas;
import resonantinduction.assemblyline.fluid.GasTank;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.builtbroken.minecraft.prefab.TileEntityAdvanced;
import com.builtbroken.minecraft.tilenetwork.ITileConnector.Connection;

/** Combustion engine that runs off of gas fluid such as methain, propain, and butane
 * 
 * @author DarkGuardsman, Archadia */
public class TileGasCombustionEngine extends TileEntityAdvanced implements IFluidHandler
{
    /** Tank which fuel is stored. */
    protected GasTank fuelTank = new GasTank(1000);
    /** Oil that that is used to keep the engine parts moving smoothly */
    protected FluidTank oilTank = new FluidTank(1000);
    /** Direction in which the engine is facing */
    protected ForgeDirection face = ForgeDirection.UP;
    float speed = 0.0f, spinUpSpeed = 20f, spinDownSpeed = 10f, maxSpeed = 1000f;
    float force = 0.0f, appliedForce;
    int animation = 0;
    //TODO balance
    int mbFuelPerTick = 10, tickPerOilDrain = 1000;
    int damage = 0, maxDamage = 10;

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (damage >= maxDamage)
        {
            //TODO go boom
        }
        if (this.ticks % tickPerOilDrain == 0)
        {
            if (this.oilTank.drain(1, false) != null)
            {
                this.oilTank.drain(1, true);
            }
            else
            {
                damage += 1;
            }
        }
        if (fuelTank.getFluid() != null && this.isValidFuel(fuelTank.getFluid()) && fuelTank.getFluidAmount() > mbFuelPerTick)
        {
            fuelTank.drain(mbFuelPerTick, true);
            speed += spinUpSpeed;
        }
        else if (speed > 0)
        {
            this.speed -= spinDownSpeed;
            if (speed < 0)
            {
                speed = 0;
            }
        }
    }

    public boolean isValidFuel(FluidStack stack)
    {
        return stack != null && stack.fluidID == FluidRegistry.getFluidID(EnumGas.METHANE.getGas().getName());
    }

    public boolean canTileConnect(Connection type, ForgeDirection dir)
    {
        if (type == Connection.FLUIDS && dir != face)
        {
            return true;
        }
        if (type == Connection.FORCE && (dir == face || dir == face.getOpposite()))
        {
            return true;
        }
        return false;
    }

    /* 8888888888888888888888888888888888888888888888888888888888888
     * Force calculation stuff
     * 888888888888888888888888888888888888888888888888888888888888 */

    //TODO re-add

    /* 8888888888888888888888888888888888888888888888888888888888888
     *Fluid Tank Stuff
     * 888888888888888888888888888888888888888888888888888888888888 */

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (from != face)
        {
            return fuelTank.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (from != face && fuelTank.getFluid() != null && resource != null && fuelTank.getFluid().isFluidEqual(resource))
        {
            return fuelTank.drain(resource.amount, doDrain);
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        if (from != face)
        {
            return fuelTank.drain(maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return from != face;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return from != face;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        if (from != face)
        {
            return new FluidTankInfo[] { fuelTank.getInfo() };
        }
        return null;
    }
}

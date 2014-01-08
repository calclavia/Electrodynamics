package com.builtbroken.assemblyline.generators;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.builtbroken.minecraft.prefab.TileEntityMachine;

/** Simple steam gen designed to burn items to create steam to power a steam device directly above
 * it. Doesn't actually make steam fluid but rather simple functions. The machines above it will
 * need to call to this machines and do a check for steam. If this machines is creating steam then
 * the machine above it should function
 * 
 * @author DarkGuardsman */
public class TileEntitySteamGen extends TileEntityMachine implements IFluidHandler
{
    /** The number of ticks that a fresh copy of the currently-burning item would keep the furnace
     * burning for */
    public int itemCookTime = 0;

    protected final int HEAT_TIME = 100;
    protected int heatTicks = 0;

    protected boolean steamMachineConnected = false, isHeated = false, creatingSteam = false;

    protected FluidTank tank = new FluidTank(2 * FluidContainerRegistry.BUCKET_VOLUME);

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        this.creatingSteam = false;
        if (itemCookTime < 10)
        {
            this.consumeFuel();
            if (itemCookTime <= 0)
            {
                if (heatTicks > 0)
                    heatTicks--;
            }
        }
        else if (this.heatTicks < HEAT_TIME)
        {
            heatTicks++;
        }
        if (this.isFunctioning())
        {
            if (this.tank != null && this.tank.getFluid() != null && this.tank.getFluidAmount() > 1 && this.tank.getFluid().isFluidEqual(new FluidStack(FluidRegistry.WATER, 1000)))
            {
                this.tank.drain(1, true);
                this.creatingSteam = true;
            }
            else
            {
                //TODO start heating up machine and blow it up if left without water for too long
                this.tank.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
            }
        }
    }

    /** Called when the generator is running low on energy and needs to burn more fuel to keep going */
    public void consumeFuel()
    {
        //TODO consume an item to keep us running
        itemCookTime += 20;
    }

    /** Is the machines running and making steam */
    public boolean isCreatingSteam()
    {
        return creatingSteam;
    }

    @Override
    public boolean canFunction()
    {
        TileEntity ent = this.worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        return super.canFunction() && ent instanceof TileEntitySteamPiston && this.itemCookTime > 0 && this.isHeated;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (resource != null && resource.getFluid().equals(FluidRegistry.WATER))
        {
            this.tank.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return fluid != null && fluid.equals(FluidRegistry.WATER);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[] { this.tank.getInfo() };
    }

}
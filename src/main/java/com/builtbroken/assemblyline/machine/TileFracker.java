package com.builtbroken.assemblyline.machine;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.assemblyline.ALRecipeLoader;
import com.builtbroken.assemblyline.fluid.EnumGas;
import com.builtbroken.assemblyline.fluid.GasTank;
import com.builtbroken.minecraft.FluidHelper;
import com.builtbroken.minecraft.interfaces.IBlockActivated;
import com.builtbroken.minecraft.prefab.TileEntityEnergyMachine;

/** @author Archadia */
public class TileFracker extends TileEntityEnergyMachine implements IFluidHandler, IBlockActivated
{
    public GasTank tank = new GasTank(10000);

    boolean autoEmpty = false;

    private Vector3 target;

    public TileFracker()
    {
        super(0, 5);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (!worldObj.isRemote)
        {
            if (this.ticks % 20 == 0)
            {
                frack();
                System.out.println("Amt: " + tank.getFluidAmount());
            }
        }
        if (!this.worldObj.isRemote && autoEmpty && this.tank != null && this.tank.getFluid() != null)
        {
            FluidHelper.fillTanksAllSides(this.worldObj, new Vector3(this), FluidHelper.getStack(this.tank.getFluid(), 600), true);
        }
    }

    public void frack()
    {
        if (target == null)
        {
            target = new Vector3(xCoord, yCoord, zCoord);
        }
        if (target.intY() > 0)
        {
            if (this.getEnergyStored() >= 4)
            {
                target.translate(Vector3.DOWN());

                int blockID = target.getBlockID(this.worldObj);
                Block block = Block.blocksList[blockID];
                if (block != null)
                {
                    if (block instanceof IFluidBlock)
                    {
                        FluidStack stack = ((IFluidBlock) block).drain(this.worldObj, target.intX(), target.intY(), target.intZ(), false);
                        if (stack != null && stack.getFluid().getID() == EnumGas.NATURAL_GAS.getGas().getID())
                        {
                            tank.fill(new FluidStack(EnumGas.NATURAL_GAS.getGas(), 1000), true);
                        }
                    }
                    worldObj.setBlockToAir(target.intX(), target.intY(), target.intZ());
                    this.consumePower(2, true);
                }
                worldObj.setBlock(target.intX(), target.intY(), target.intZ(), ALRecipeLoader.frackingPipe.blockID);
                this.consumePower(500, true);
            }
        }
    }

    @Override
    public boolean onActivated(EntityPlayer entityPlayer)
    {
        entityPlayer.addChatMessage("Gas: " + tank.getFluidAmount());
        return true;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (resource != null)
        {
            if (tank != null && tank.getFluid() != null && tank.getFluid().containsFluid(resource))
            {
                return tank.drain(resource.amount, true);
            }
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return this.tank != null && this.tank.getFluid() != null ? this.drain(from, FluidHelper.getStack(this.tank.getFluid(), maxDrain), doDrain) : null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return fluid != null && this.tank != null && this.tank.getFluid() != null && this.tank.getFluid().getFluid().equals(fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[] { this.tank.getInfo() };
    }
}
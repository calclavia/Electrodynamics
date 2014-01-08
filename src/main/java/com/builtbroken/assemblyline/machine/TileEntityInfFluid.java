package com.builtbroken.assemblyline.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.lang.TextHelper.TextColor;
import com.builtbroken.minecraft.FluidHelper;
import com.builtbroken.minecraft.interfaces.IBlockActivated;

public class TileEntityInfFluid extends TileEntity implements IFluidHandler, IBlockActivated
{
    FluidTank tank = new FluidTank(Integer.MAX_VALUE);
    boolean autoEmpty = false;

    @Override
    public void updateEntity()
    {
        if (!this.worldObj.isRemote && autoEmpty && this.tank != null && this.tank.getFluid() != null)
        {
            FluidHelper.fillTanksAllSides(this.worldObj, new Vector3(this), FluidHelper.getStack(this.tank.getFluid(), 600), true);
        }
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
            if (tank == null || tank.getFluid() == null)
            {
                tank = new FluidTank(Integer.MAX_VALUE);
                tank.setFluid(new FluidStack(FluidRegistry.WATER, Integer.MAX_VALUE));
            }
            if (tank != null && tank.getFluid() != null && tank.getFluid().containsFluid(resource))
            {
                return resource.copy();
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

    @Override
    public boolean onActivated(EntityPlayer entityPlayer)
    {
        if (entityPlayer != null && entityPlayer.getHeldItem() != null)
        {
            ItemStack held = entityPlayer.getHeldItem();
            if (held.itemID == Item.stick.itemID)
            {
                if (!this.worldObj.isRemote)
                {
                    this.autoEmpty = !autoEmpty;
                    entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("AutoPump > " + (autoEmpty ? TextColor.DARKGREEN + "On" : TextColor.RED + "Off")));
                }
                return true;
            }
            FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(held);
            if (fluidStack != null)
            {
                if (!this.worldObj.isRemote)
                {
                    fluidStack = fluidStack.copy();
                    fluidStack.amount = Integer.MAX_VALUE;
                    this.tank.setFluid(fluidStack);
                    entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Fluid Set to > " + fluidStack.getFluid().getName()));

                }
                return true;
            }
        }
        return false;
    }

}

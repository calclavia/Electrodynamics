package com.builtbroken.minecraft.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Items that are used as molds by the mold-injector to create items from liquid materials. Eg iron
 * armor from molten iron fluid
 * 
 * @author Darkguardsman */
public interface IInjectorMold
{
    public ItemStack getOutput(FluidStack fluid, ItemStack mold);

    public FluidStack getRequirement(ItemStack mold);
}

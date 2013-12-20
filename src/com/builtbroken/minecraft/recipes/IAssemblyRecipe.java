package com.builtbroken.minecraft.recipes;

import net.minecraft.item.ItemStack;

/** WIP feature to allow an item/block to be slowly built one step at a time. Object can be an
 * ItemStack, Entity, or even an object just for this purpose. Though if its not world based you'll
 * need to inform the assembler that it exists
 * 
 * @author Darkgaurdsman */
public interface IAssemblyRecipe
{
    /** Cost in materials(ItemStack) to complete the next step in the build process */
    public ItemStack[] getCostAtStep(Object object, int step);

    /** Number of steps to complete the crafting */
    public int getSteps(Object object);

    public void nextStep(Object Object);
}

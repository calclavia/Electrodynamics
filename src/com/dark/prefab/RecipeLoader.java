package com.dark.prefab;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Recipe system to make it easier to load recipes for a mod
 * 
 * @author DarkGuardsman */
public abstract class RecipeLoader
{
    protected static Object circuit;
    protected static Object circuit2;
    protected static Object circuit3;
    protected static Object steel;
    protected static Object steelPlate;
    protected static Object motor;
    protected static Object bronze;
    protected static Object bronzePlate;
    protected static Object copper;
    protected static Object copperPlate;

    static boolean loaded = false;

    /** Should be called to load recipes. The main class only loads ore name items to decrease
     * chances of missing items in recipes */
    public void loadRecipes()
    {
        if (!loaded)
        {
            /* Vinalla items load first */
            circuit = Item.redstoneRepeater;
            circuit2 = Item.comparator;
            steel = Item.ingotIron;
            steelPlate = Item.ingotGold;
            copper = Item.ingotIron;
            copperPlate = Item.ingotGold;
            motor = Block.pistonBase;
            bronze = Item.ingotIron;
            bronzePlate = Item.ingotGold;
            /* Ore directory items load over the vinalla ones if they are present */
            if (OreDictionary.getOres("basicCircuit").size() > 0)
            {
                circuit = "basicCircuit";
            }
            if (OreDictionary.getOres("advancedCircuit").size() > 0)
            {
                circuit = "advancedCircuit";
            }
            if (OreDictionary.getOres("ingotSteel").size() > 0)
            {
                steel = "ingotSteel";
            }
            if (OreDictionary.getOres("plateSteel").size() > 0)
            {
                steelPlate = "plateSteel";
            }
            if (OreDictionary.getOres("motor").size() > 0)
            {
                motor = "motor";
            }
            if (OreDictionary.getOres("ingotBronze").size() > 0)
            {
                bronze = "ingotBronze";
            }
            if (OreDictionary.getOres("plateBronze").size() > 0)
            {
                bronzePlate = "plateBronze";
            }
            if (OreDictionary.getOres("copperBronze").size() > 0)
            {
                bronze = "copperBronze";
            }
            if (OreDictionary.getOres("copperBronze").size() > 0)
            {
                bronzePlate = "copperBronze";
            }
        }
    }

    public ItemStack setStackSize(ItemStack stack, int amount)
    {
        if (stack != null)
        {
            ItemStack itemStack = stack.copy();
            itemStack.stackSize = amount;
            return itemStack;
        }
        return stack;
    }
}

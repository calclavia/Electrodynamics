package com.dark;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class IndustryCreativeTab extends CreativeTabs
{
    public ItemStack itemStack = new ItemStack(Item.ingotIron, 1, 0);

    private static IndustryCreativeTab tabAutomation = new IndustryCreativeTab("Automation");
    private static IndustryCreativeTab tabIndustrial = new IndustryCreativeTab("Industrial");
    private static IndustryCreativeTab tabHydrualic = new IndustryCreativeTab("Hydraulic");
    private static IndustryCreativeTab tabMining = new IndustryCreativeTab("Mining");

    public IndustryCreativeTab(String label)
    {
        super(label);
    }

    @Override
    public ItemStack getIconItemStack()
    {
        return this.itemStack;
    }

    public void setIconItemStack(ItemStack stack)
    {
        this.itemStack = stack;
    }

    public static IndustryCreativeTab tabAutomation()
    {
        if (tabAutomation == null)
        {
            tabAutomation = new IndustryCreativeTab("Automation");
        }
        return tabAutomation;
    }

    public static IndustryCreativeTab tabIndustrial()
    {
        if (tabIndustrial == null)
        {
            tabIndustrial = new IndustryCreativeTab("Industrial");
        }
        return tabIndustrial;
    }

    public static IndustryCreativeTab tabHydraulic()
    {
        if (tabHydrualic == null)
        {
            tabHydrualic = new IndustryCreativeTab("Hydraulic");
        }
        return tabHydrualic;
    }

    public static IndustryCreativeTab tabMining()
    {
        if (tabMining == null)
        {
            tabMining = new IndustryCreativeTab("Mining");
        }
        return tabMining;
    }

}

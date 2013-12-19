package com.dark;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class IndustryTabs extends CreativeTabs
{
    public ItemStack itemStack = new ItemStack(Item.ingotIron, 1, 0);

    private static IndustryTabs tabAutomation = new IndustryTabs("Automation");
    private static IndustryTabs tabIndustrial = new IndustryTabs("Industrial");
    private static IndustryTabs tabHydrualic = new IndustryTabs("Hydraulic");
    private static IndustryTabs tabMining = new IndustryTabs("Mining");

    public IndustryTabs(String label)
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

    public static IndustryTabs tabAutomation()
    {
        if (tabAutomation == null)
        {
            tabAutomation = new IndustryTabs("Automation");
        }
        return tabAutomation;
    }

    public static IndustryTabs tabIndustrial()
    {
        if (tabIndustrial == null)
        {
            tabIndustrial = new IndustryTabs("Industrial");
        }
        return tabIndustrial;
    }

    public static IndustryTabs tabHydraulic()
    {
        if (tabHydrualic == null)
        {
            tabHydrualic = new IndustryTabs("Hydraulic");
        }
        return tabHydrualic;
    }

    public static IndustryTabs tabMining()
    {
        if (tabMining == null)
        {
            tabMining = new IndustryTabs("Mining");
        }
        return tabMining;
    }

}

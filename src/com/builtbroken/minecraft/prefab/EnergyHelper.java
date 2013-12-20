package com.builtbroken.minecraft.prefab;


import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;

public class EnergyHelper
{

    /** Recharges electric item. */
    public static void recharge(ItemStack itemStack, TileEntityEnergyMachine machine)
    {
        if (itemStack != null)
        {
            if (itemStack.getItem() instanceof IItemElectric)
            {
                machine.setEnergyStored(machine.getEnergyStored() - ElectricItemHelper.chargeItem(itemStack, machine.getProvide(ForgeDirection.UNKNOWN)));

            }
            else if (itemStack.getItem() instanceof ISpecialElectricItem)
            {
                ISpecialElectricItem electricItem = (ISpecialElectricItem) itemStack.getItem();
                IElectricItemManager manager = electricItem.getManager(itemStack);
                float energy = Math.max(machine.getProvide(ForgeDirection.UNKNOWN) * Compatibility.IC2_RATIO, 0);
                energy = manager.charge(itemStack, (int) (energy * Compatibility.TO_IC2_RATIO), 0, false, false) * Compatibility.IC2_RATIO;
                machine.provideElectricity(energy, true);
            }
        }
    }

    /** Discharges electric item. */
    public static void discharge(ItemStack itemStack, TileEntityEnergyMachine machine)
    {
        if (itemStack != null)
        {
            if (itemStack.getItem() instanceof IItemElectric)
            {
                machine.setEnergyStored(machine.getEnergyStored() + ElectricItemHelper.dischargeItem(itemStack, machine.getRequest(ForgeDirection.UNKNOWN)));

            }
            else if (itemStack.getItem() instanceof ISpecialElectricItem)
            {
                ISpecialElectricItem electricItem = (ISpecialElectricItem) itemStack.getItem();

                if (electricItem.canProvideEnergy(itemStack))
                {
                    IElectricItemManager manager = electricItem.getManager(itemStack);
                    float energy = Math.max(machine.getRequest(ForgeDirection.UNKNOWN) * Compatibility.IC2_RATIO, 0);
                    energy = manager.discharge(itemStack, (int) (energy * Compatibility.TO_IC2_RATIO), 0, false, false);
                    machine.receiveElectricity(energy, true);
                }
            }
        }
    }

    public static boolean isBatteryItem(ItemStack itemStack)
    {
        if (itemStack != null)
        {
            if (itemStack.getItem() instanceof IItemElectric)
            {
                return true;
            }
            else if (itemStack.getItem() instanceof ISpecialElectricItem)
            {
                ISpecialElectricItem electricItem = (ISpecialElectricItem) itemStack.getItem();

                if (electricItem.canProvideEnergy(itemStack))
                {
                    return true;
                }
            }
        }
        return false;
    }
}

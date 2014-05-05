package resonantinduction.core.prefab.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import resonantinduction.electrical.battery.TileBattery;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.item.IEnergyItem;
import universalelectricity.api.item.IVoltageItem;
import calclavia.lib.render.EnumColor;
import calclavia.lib.utility.LanguageUtility;

/** Prefab for all eletric based tools
 * 
 * @author DarkGurdsman */
public class ItemEnergyTool extends ItemTool implements IEnergyItem, IVoltageItem
{
    /** Default battery size */
    protected long batterySize = 500000;
    /** Does this item support energy tiers */
    protected boolean hasTier = false;
    /** Display energy in tool tips */
    protected boolean showEnergy = true;
    /** Number of energy tiers */
    protected int energyTiers = 0;

    public ItemEnergyTool(int id)
    {
        super(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
    {
        if (hasTier)
            list.add(LanguageUtility.getLocal("tooltip.tier") + ": " + (getTier(itemStack) + 1));

        if (showEnergy)
        {
            String color = "";
            long joules = this.getEnergy(itemStack);

            if (joules <= this.getEnergyCapacity(itemStack) / 3)
            {
                color = "\u00a74";
            }
            else if (joules > this.getEnergyCapacity(itemStack) * 2 / 3)
            {
                color = "\u00a72";
            }
            else
            {
                color = "\u00a76";
            }
            itemStack.getItemDamageForDisplay();
            list.add(LanguageUtility.getLocal("tooltip.battery.energy").replace("%0", color).replace("%1", EnumColor.GREY.toString()).replace("%v0", UnitDisplay.getDisplayShort(joules, Unit.JOULES)).replace("%v1", UnitDisplay.getDisplayShort(this.getEnergyCapacity(itemStack), Unit.JOULES)));
        }
    }

    @Override
    public void onCreated(ItemStack itemStack, World world, EntityPlayer player)
    {
        super.onCreated(itemStack, world, player);
        this.setEnergy(itemStack, 0);
    }

    @Override
    public long recharge(ItemStack itemStack, long energy, boolean doReceive)
    {
        long rejectedElectricity = Math.max((this.getEnergy(itemStack) + energy) - this.getEnergyCapacity(itemStack), 0);
        long energyToReceive = Math.min(energy - rejectedElectricity, getTransferRate(itemStack));

        if (doReceive)
        {
            this.setEnergy(itemStack, this.getEnergy(itemStack) + energyToReceive);
        }

        return energyToReceive;
    }

    @Override
    public long discharge(ItemStack itemStack, long energy, boolean doTransfer)
    {
        long energyToExtract = Math.min(Math.min(this.getEnergy(itemStack), energy), getTransferRate(itemStack));

        if (doTransfer)
        {
            this.setEnergy(itemStack, this.getEnergy(itemStack) - energyToExtract);
        }

        return energyToExtract;
    }

    @Override
    public long getVoltage(ItemStack itemStack)
    {
        return UniversalElectricity.DEFAULT_VOLTAGE;
    }

    @Override
    public void setEnergy(ItemStack itemStack, long joules)
    {
        if (itemStack.getTagCompound() == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        long electricityStored = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0);
        itemStack.getTagCompound().setLong("electricity", electricityStored);
    }

    public long getTransfer(ItemStack itemStack)
    {
        return this.getEnergyCapacity(itemStack) - this.getEnergy(itemStack);
    }

    /** Gets the energy stored in the item. Energy is stored using item NBT */
    @Override
    public long getEnergy(ItemStack itemStack)
    {
        if (itemStack.getTagCompound() == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        long energyStored = itemStack.getTagCompound().getLong("electricity");
        return energyStored;
    }

    @Override
    public int getDisplayDamage(ItemStack stack)
    {
        return (int) (100 - ((double) this.getEnergy(stack) / (double) getEnergyCapacity(stack)) * 100);
    }

    @Override
    public long getEnergyCapacity(ItemStack theItem)
    {
        return TileBattery.getEnergyForTier(getTier(theItem));
    }

    public long getTransferRate(ItemStack itemStack)
    {
        return this.getEnergyCapacity(itemStack) / 100;
    }

    public static ItemStack setTier(ItemStack itemStack, int tier)
    {
        if (itemStack.getTagCompound() == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setByte("tier", (byte) tier);
        return itemStack;
    }

    public static byte getTier(ItemStack itemStack)
    {
        if (itemStack.getTagCompound() == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        return itemStack.getTagCompound().getByte("tier");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int i = 0; i >= 0 && i < this.energyTiers; i++)
        {
            par3List.add(CompatibilityModule.getItemWithCharge(setTier(new ItemStack(this), i), 0));
            par3List.add(CompatibilityModule.getItemWithCharge(setTier(new ItemStack(this), i), this.getEnergyCapacity(setTier(new ItemStack(this), i))));
        }
    }
}

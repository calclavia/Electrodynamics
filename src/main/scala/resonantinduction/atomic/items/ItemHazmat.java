package resonantinduction.atomic.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.EnumHelper;
import resonant.api.armor.IAntiPoisonArmor;
import resonantinduction.atomic.Atomic;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantTab;

/** Hazmat */
public class ItemHazmat extends ItemArmor implements IAntiPoisonArmor
{

    public static final ItemArmor.ArmorMaterial hazmatArmorMaterial = EnumHelper.addArmorMaterial("HAZMAT", 0, new int[]{0, 0, 0, 0}, 0);
    public ItemHazmat(String name, int slot)
    {
        super(hazmatArmorMaterial, Atomic.proxy().getArmorIndex("hazmat"), slot);
        this.setUnlocalizedName(Reference.prefix() + name);
        this.setCreativeTab(ResonantTab.tab());
        this.setMaxDamage(200000);
    }

    @Override
    public Item setUnlocalizedName(String par1Str)
    {
        super.setUnlocalizedName(par1Str);
        this.setTextureName(par1Str);
        return this;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
    {
        return Reference.prefix() + Reference.modelPath() + "hazmat.png";
    }

    @Override
    public boolean isProtectedFromPoison(ItemStack itemStack, EntityLivingBase entityLiving, String type)
    {
        return type.equalsIgnoreCase("radiation") || type.equalsIgnoreCase("chemical") || type.equalsIgnoreCase("contagious");
    }

    @Override
    public void onProtectFromPoison(ItemStack itemStack, EntityLivingBase entityLiving, String type)
    {
        itemStack.damageItem(1, entityLiving);
    }

    @Override
    public int getArmorType()
    {
        return this.armorType;
    }

    @Override
    public boolean isPartOfSet(ItemStack armorStack, ItemStack compareStack)
    {
        if(armorStack != null && compareStack != null)
        {
            return armorStack.getItem().equals(compareStack.getItem());
        }
        return false;
    }

    @Override
    public boolean areAllPartsNeeded(ItemStack armorStack, EntityLivingBase entity, DamageSource source, Object... data)
    {
        return true;
    }
}

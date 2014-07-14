package resonantinduction.atomic.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import resonant.api.armor.IAntiPoisonArmor;
import resonantinduction.core.Reference;
import resonantinduction.core.TabRI;

/** Hazmat */
public class ItemHazmat extends ItemArmor implements IAntiPoisonArmor
{
    public ItemHazmat(int id, EnumArmorMaterial material, int index, int slot)
    {
        super(id, material, index, slot);
        this.setCreativeTab(TabRI.DEFAULT);
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
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer)
    {
        return Reference.PREFIX + Reference.MODEL_PATH + "hazmat.png";
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

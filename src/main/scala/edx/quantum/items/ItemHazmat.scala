package edx.quantum.items

import edx.core.{EDXCreativeTab, Electrodynamics, Reference}
import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.item.{Item, ItemArmor, ItemStack}
import net.minecraft.util.DamageSource
import net.minecraftforge.common.util.EnumHelper
import resonant.api.armor.IAntiPoisonArmor

/**
 * Hazmat
 */
object ItemHazmat
{
  final val hazmatArmorMaterial: ItemArmor.ArmorMaterial = EnumHelper.addArmorMaterial("HAZMAT", 0, Array[Int](0, 0, 0, 0), 0)
}

class ItemHazmat(slot: Int) extends ItemArmor(ItemHazmat.hazmatArmorMaterial, Electrodynamics.proxy.getArmorIndex("hazmat"), slot) with IAntiPoisonArmor
{
  def this(name: String, slot: Int)
  {
    this(slot)
    this.setUnlocalizedName(Reference.prefix + name)
    this.setCreativeTab(EDXCreativeTab)
    this.setMaxDamage(200000)
  }

  override def setUnlocalizedName(par1Str: String): Item =
  {
    super.setUnlocalizedName(par1Str)
    this.setTextureName(par1Str)
    return this
  }

  override def getArmorTexture(stack: ItemStack, entity: Entity, slot: Int, `type`: String): String =
  {
    return Reference.prefix + Reference.modelPath + "hazmat.png"
  }

  def isProtectedFromPoison(itemStack: ItemStack, entityLiving: EntityLivingBase, `type`: String): Boolean =
  {
    return `type`.equalsIgnoreCase("radiation") || `type`.equalsIgnoreCase("chemical") || `type`.equalsIgnoreCase("contagious")
  }

  def onProtectFromPoison(itemStack: ItemStack, entityLiving: EntityLivingBase, `type`: String)
  {
    itemStack.damageItem(1, entityLiving)
  }

  def getArmorType: Int =
  {
    return this.armorType
  }

  def isPartOfSet(armorStack: ItemStack, compareStack: ItemStack): Boolean =
  {
    if (armorStack != null && compareStack != null)
    {
      return armorStack.getItem == compareStack.getItem
    }
    return false
  }

  def areAllPartsNeeded(armorStack: ItemStack, entity: EntityLivingBase, source: DamageSource, data: AnyRef*): Boolean =
  {
    return true
  }
}
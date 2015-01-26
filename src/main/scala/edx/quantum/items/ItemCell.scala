package edx.quantum.items

import edx.core.{EDXCreativeTab, Reference}
import edx.quantum.QuantumContent
import net.minecraft.item.ItemStack
import resonantengine.lib.prefab.item.ItemTooltip
import resonantengine.lib.utility.LanguageUtility

class ItemCell extends ItemTooltip
{
  //Constructor
  setContainerItem(QuantumContent.itemCell)

  def this(name: String)
  {
    this()
    if (!name.equalsIgnoreCase("cellEmpty")) this.setContainerItem(QuantumContent.itemCell)
    this.setUnlocalizedName(Reference.prefix + name)
    this.setTextureName(Reference.prefix + name)
    setCreativeTab(EDXCreativeTab)
  }

  override def getUnlocalizedName(itemstack: ItemStack): String =
  {
    val localized: String = LanguageUtility.getLocal(getUnlocalizedName() + "." + itemstack.getItemDamage + ".name")
    if (localized != null && !localized.isEmpty)
    {
      return getUnlocalizedName() + "." + itemstack.getItemDamage
    }
    return getUnlocalizedName()
  }
}
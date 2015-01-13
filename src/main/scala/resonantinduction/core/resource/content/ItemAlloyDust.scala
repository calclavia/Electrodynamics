package resonantinduction.core.resource.content

import net.minecraft.item.{Item, ItemStack}
import resonantinduction.core.Reference
import resonantinduction.core.resource.alloy.{AlloyUtility, TAlloyItem}

/**
 * An alloy dust is a dust that contains mixed metals
 * TODO: Considering combining the alloy with the non-alloy dusts via NBT
 * @author Calclavia
 */
class ItemAlloyDust extends Item with TAlloyItem
{
  setCreativeTab(null)
  setTextureName(Reference.prefix + "oreDust")
  setUnlocalizedName(Reference.prefix + "alloyDust")

  override def getColorFromItemStack(itemStack: ItemStack, p_82790_2_ : Int): Int = AlloyUtility.getAlloy(itemStack).color

}

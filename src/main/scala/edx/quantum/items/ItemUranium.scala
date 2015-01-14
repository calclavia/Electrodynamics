package edx.quantum.items

import java.util.List

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import org.lwjgl.input.Keyboard
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._

/**
 * Uranium
 */
class ItemUranium extends ItemRadioactive
{
  //Constructor
  this.setHasSubtypes(true)
  this.setMaxDamage(0)

  override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
    val tooltip: String = LanguageUtility.getLocal(getUnlocalizedName(itemStack) + ".tooltip")
    if (tooltip != null && tooltip.length > 0)
    {
      if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
      {
        list.add(LanguageUtility.getLocal("tooltip.noShift").replace("#0", EnumColor.AQUA.toString).replace("#1", EnumColor.GREY.toString))
      }
      else
      {
        list.addAll(LanguageUtility.splitStringPerWord(tooltip, 5))
      }
    }
  }

  override def getUnlocalizedName(itemStack: ItemStack): java.lang.String =
  {
    return super.getUnlocalizedName() + "." + itemStack.getItemDamage
  }

  override def getSubItems(item: Item, par2CreativeTabs: CreativeTabs, list: List[_])
  {
    list.add(new ItemStack(item, 1, 0))
    list.add(new ItemStack(item, 1, 1))
  }
}
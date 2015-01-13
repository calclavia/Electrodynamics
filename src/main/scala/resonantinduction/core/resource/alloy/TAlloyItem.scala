package resonantinduction.core.resource.alloy

import java.util

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import resonant.lib.render.EnumColor
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.StringWrapper._
import resonant.lib.wrapper.WrapList._

/**
 * A trait applied to all items that can store alloys within them
 * @author Calclavia
 */
trait TAlloyItem extends Item
{
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[_], par4: Boolean)
  {
    super.addInformation(itemStack, player, list, par4)

    val nbt = NBTUtility.getNBTTagCompound(itemStack)
    if (nbt.hasKey("mixed"))
      list.add("Mixed: " + nbt.getBoolean("mixed"))
    val alloy = new Alloy(nbt)
    alloy.content.map(c => EnumColor.ORANGE + c._1.capitalizeFirst + EnumColor.DARK_RED + " " + Math.round(alloy.percentage(c._1) * 100) + "%").foreach(m => list.add(m))
  }
}

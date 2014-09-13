package resonantinduction.core.util

import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

/**
 * General Utility
 * @author Calclavia
 */
object ResonantUtil
{
  val dyes = Array("dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite")

  def isDye(is: ItemStack): Int =
  {
    return (0 until dyes.size) find (i => OreDictionary.getOreID(is) != -1 && (OreDictionary.getOreName(OreDictionary.getOreID(is)) == dyes(i))) getOrElse (-1)
  }
}

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
  val dyeColors = Array[Int](1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 11250603, 4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844, 15790320)

  def isDye(is: ItemStack): Int =
  {
    return (0 until dyes.size) find (i => OreDictionary.getOreID(is) != -1 && (OreDictionary.getOreName(OreDictionary.getOreID(is)) == dyes(i))) getOrElse (-1)
  }

  /**
   * Gets the color hex code from the color's ID
   */
  def getColorHex(id: Int): Int = dyeColors(id)
}

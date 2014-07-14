package resonantinduction.electrical.em

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import cpw.mods.fml.relauncher.{SideOnly, Side}
import scala.collection.convert.wrapAsScala._

/**
 * @author Calclavia
 */
object TabEC extends CreativeTabs(CreativeTabs.getNextID, "ec")
{
  override def getTabIconItem = new ItemStack(ElectromagneticCoherence.blockLaserEmitter).getItem

  @SideOnly(Side.CLIENT)
  override def displayAllReleventItems(list: java.util.List[_])
  {
    super.displayAllReleventItems(list)
    def add[T](list: java.util.List[T], value: Any) = list.add(value.asInstanceOf[T])
    add(list, ElectromagneticCoherence.guideBook)
  }
}

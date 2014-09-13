package resonantinduction.core.prefab.part

import codechicken.multipart.{IRedstonePart, TMultiPart}
import net.minecraft.item.ItemStack
import net.minecraft.util.MovingObjectPosition
import resonant.content.spatial.block.TraitTicker

import scala.collection.convert.wrapAll._
import scala.collection.mutable

trait TPart extends TMultiPart with TraitTicker
{
  override def update()
  {
    super[TraitTicker].update()
  }

  protected def getItem: ItemStack

  protected def getDrops(drops: mutable.Set[ItemStack])
  {
    drops += getItem
  }

  override def getDrops: java.lang.Iterable[ItemStack] =
  {
    val drops = mutable.Set.empty[ItemStack]
    getDrops(drops)
    return drops
  }

  override def pickItem(hit: MovingObjectPosition): ItemStack =
  {
    return getItem
  }

  protected def checkRedstone(side: Int): Boolean =
  {
    if (this.world.isBlockIndirectlyGettingPowered(x, y, z))
    {
      return true
    }
    else
    {
      for (tp <- tile.partList)
      {
        if (tp.isInstanceOf[IRedstonePart])
        {
          val rp: IRedstonePart = tp.asInstanceOf[IRedstonePart]
          if ((Math.max(rp.strongPowerLevel(side), rp.weakPowerLevel(side)) << 4) > 0)
          {
            return true
          }
        }
      }
    }
    return false
  }

  override def toString: String =  "[" + getClass.getSimpleName + "]" + x + "x " + y + "y " + z + "z"
}
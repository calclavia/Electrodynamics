package resonantinduction.core.prefab.part

import java.util.{ArrayList, List}

import codechicken.multipart.{IRedstonePart, TMultiPart}
import net.minecraft.item.ItemStack
import net.minecraft.util.MovingObjectPosition
import resonant.content.spatial.block.TraitTicker

import scala.collection.JavaConversions._

trait TraitPart extends TMultiPart with TraitTicker
{
  protected def getItem: ItemStack

  override def getDrops: Iterable[ItemStack] =
  {
    val drops: List[ItemStack] = new ArrayList[ItemStack]
    drops.add(getItem)
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
}
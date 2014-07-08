package mffs.field.module

import java.util.Set

import mffs.base.ItemModule
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.BlockFluidBase
import resonant.api.mffs.IProjector
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

class ItemModuleSponge extends ItemModule
{
  setMaxStackSize(1)

  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    if (projector.getTicks % 60 == 0)
    {
      val world = projector.asInstanceOf[TileEntity].getWorldObj

      if (!world.isRemote)
      {
        for (point <- projector.getInteriorPoints)
        {
          val block = point.getBlock(world)

          if (block.isInstanceOf[BlockLiquid] || block.isInstanceOf[BlockFluidBase])
          {
            point.setBlock(world, Blocks.air)
          }
        }
      }
    }

    return super.onProject(projector, fields)
  }

  override def requireTicks(moduleStack: ItemStack): Boolean =
  {
    return true
  }
}
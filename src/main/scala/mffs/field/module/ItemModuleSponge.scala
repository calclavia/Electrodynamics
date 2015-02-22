package mffs.field.module

import java.util.Set

import mffs.base.ItemModule

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

	override def requireTicks(moduleStack: Item): Boolean =
  {
    return true
  }
}
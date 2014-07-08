package mffs.mobilize.event

import mffs.base.TileMFFSInventory
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.world.World
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

class BlockInventoryDropDelayedEvent(handler: IDelayedEventHandler, ticks: Int, block: Block, world: World, position: Vector3, projector: TileMFFSInventory) extends BlockDropDelayedEvent(handler, ticks, block, world, position)
{
  protected override def onEvent
  {
    if (!world.isRemote)
    {
      if (position.getBlock(this.world) == block)
      {
        val itemStacks = block.getDrops(this.world, this.position.xi, this.position.yi, this.position.zi, this.position.getBlockMetadata(world), 0)

        for (itemStack <- itemStacks)
        {
          projector.mergeIntoInventory(itemStack)
        }

        position.setBlock(world, Blocks.air)
      }
    }
  }
}
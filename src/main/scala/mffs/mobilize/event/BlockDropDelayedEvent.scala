package mffs.mobilize.event

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.world.World
import universalelectricity.core.transform.vector.Vector3

class BlockDropDelayedEvent(handler: IDelayedEventHandler, ticks: Int, block: Block, world: World, position: Vector3) extends DelayedEvent(handler, ticks)
{
  protected override def onEvent
  {
    if (!this.world.isRemote)
    {
      if (this.position.getBlock(this.world) eq this.block)
      {
        this.block.dropBlockAsItem(this.world, this.position.xi, this.position.yi, this.position.zi, this.position.getBlockMetadata(world), 0)
        this.position.setBlock(this.world, Blocks.air)
      }
    }
  }
}
package mffs.field.mobilize.event

class BlockDropDelayedEvent(handler: IDelayedEventHandler, ticks: Int, block: Block, world: World, position: Vector3d) extends DelayedEvent(handler, ticks)
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
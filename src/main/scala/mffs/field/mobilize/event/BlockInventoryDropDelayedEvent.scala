package mffs.field.mobilize.event

import mffs.base.TileMFFSInventory

class BlockInventoryDropDelayedEvent(handler: IDelayedEventHandler, ticks: Int, block: Block, world: World, position: Vector3d, projector: TileMFFSInventory)
	extends BlockDropDelayedEvent(handler, ticks, block, world, position)
{
  protected override def onEvent
  {
	  if (Game.instance.networkManager.isServer)
    {
      if (position.getBlock(this.world) == block)
      {
		  val Items = block.getDrops(this.world, this.position.xi, this.position.yi, this.position.zi, this.position.getBlockMetadata(world), 0)

		  for (Item <- Items)
        {
			projector.mergeIntoInventory(Item)
        }

        position.setBlock(world, Blocks.air)
      }
    }
  }
}
package mffs.field.mobilize.event

import mffs.field.mobilize.BlockMobilizer

/**
 * Removes the TileEntity
 *
 * @author Calclavia
 */
class BlockPreMoveDelayedEvent(_handler: IDelayedEventHandler, _ticks: Int, val startPosition: VectorWorld, val newPosition: VectorWorld) extends DelayedEvent(_handler, _ticks)
{
  protected override def onEvent
  {
    if (!startPosition.world.isRemote)
    {
		if ((handler.asInstanceOf[BlockMobilizer]).canMove(startPosition, newPosition))
      {
        val tileEntity = startPosition.getTileEntity
        val evt = new EventForceMobilize.EventPreForceManipulate(startPosition.world, startPosition.xi, startPosition.yi, startPosition.zi, newPosition.xi, newPosition.yi, newPosition.zi)
        MinecraftForge.EVENT_BUS.post(evt)

        if (!evt.isCanceled)
        {
          val blockID = startPosition.getBlock
          val blockMetadata = startPosition.getBlockMetadata
          MovementUtility.setBlockSneaky(startPosition.world, this.startPosition, Blocks.air, 0, null)
          val tileData: NBTTagCompound = new NBTTagCompound
          if (tileEntity != null)
          {
            tileEntity.writeToNBT(tileData)
          }

          handler.queueEvent(new BlockPostMoveDelayedEvent(handler, 0, this.startPosition, this.newPosition, blockID, blockMetadata, tileEntity, tileData))
        }
      }
      else
      {
		  handler.asInstanceOf[BlockMobilizer].failedPositions.add(startPosition)
		  handler.asInstanceOf[BlockMobilizer].markFailMove()
		  handler.asInstanceOf[BlockMobilizer].clearQueue()
      }
    }
  }
}
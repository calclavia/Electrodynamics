package mffs.mobilize.event

import mffs.mobilize.TileForceMobilizer
import resonant.api.mffs.EventForceManipulate
import resonant.api.mffs.EventForceManipulate.EventPreForceManipulate
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import resonant.lib.utility.MovementUtility
import universalelectricity.core.transform.vector.Vector3
import universalelectricity.core.transform.vector.VectorWorld

/**
 * Removes the TileEntity
 *
 * @author Calclavia
 */
class BlockPreMoveDelayedEvent(_handler: IDelayedEventHandler, _ticks: Int,  val startPosition: VectorWorld, val newPosition: VectorWorld) extends DelayedEvent(_handler, _ticks)
{
  protected override def onEvent
  {
    if (!this.world.isRemote)
    {
      if ((handler.asInstanceOf[TileForceMobilizer]).canMove(new VectorWorld(world, startPosition), newPosition))
      {
        val tileEntity = startPosition.getTileEntity
        val evt= new EventForceManipulate.EventPreForceManipulate(this.world, this.startPosition.xi, this.startPosition.yi, this.startPosition.zi, this.newPosition.xi, this.newPosition.yi, this.newPosition.zi)
        MinecraftForge.EVENT_BUS.post(evt)
        if (!evt.isCanceled)
        {
          val blockID: Int = this.startPosition.getBlock(this.world)
          val blockMetadata: Int = this.startPosition.getBlockMetadata(this.world)
          MovementUtility.setBlockSneaky(this.world, this.startPosition, 0, 0, null)
          val tileData: NBTTagCompound = new NBTTagCompound
          if (tileEntity != null)
          {
            tileEntity.writeToNBT(tileData)
          }
          handler.queueEvent(new BlockPostMoveDelayedEvent(handler, 0, this.world, this.startPosition, this.newPosition, blockID, blockMetadata, tileEntity, tileData))
        }
      }
      else
      {
        (handler.asInstanceOf[TileForceMobilizer]).markFailMove = true
      }
    }
  }
}
package mffs.field.mobilize.event

import java.lang.reflect.Method

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import resonant.api.mffs.EventForceManipulate
import resonant.lib.utility.MovementUtility
import universalelectricity.core.transform.vector.VectorWorld

/**
 * Sets the new position into the original TileEntities' block.
 *
 * @author Calclavia
 */
class BlockPostMoveDelayedEvent(_handler: IDelayedEventHandler, _ticks: Int, startPosition: VectorWorld, newPosition: VectorWorld, block: Block, blockMetadata: Int, tileEntity: TileEntity, tileData: NBTTagCompound) extends DelayedEvent(_handler, _ticks)
{
  protected override def onEvent
  {
    println("call")
    if (!startPosition.world.isRemote)
    {
      if (block != Blocks.air)
      {
        try
        {
          if (this.tileEntity != null && this.tileData != null)
          {
            val isMultipart: Boolean = this.tileData.getString("id") == "savedMultipart"
            var newTile: TileEntity = null
            if (isMultipart)
            {
              try
              {
                val multipart: Class[_] = Class.forName("codechicken.multipart.MultipartHelper")
                val m: Method = multipart.getMethod("createTileFromNBT", classOf[World], classOf[NBTTagCompound])
                newTile = m.invoke(null, startPosition.world, this.tileData).asInstanceOf[TileEntity]
              }
              catch
                {
                  case e: Exception =>
                  {
                    e.printStackTrace
                  }
                }
            }
            else
            {
              newTile = TileEntity.createAndLoadEntity(this.tileData)
            }
            MovementUtility.setBlockSneaky(newPosition.world, newPosition, block, this.blockMetadata, newTile)
            if (newTile != null && isMultipart)
            {
              try
              {
                val multipart: Class[_] = Class.forName("codechicken.multipart.MultipartHelper")
                multipart.getMethod("sendDescPacket", classOf[World], classOf[TileEntity]).invoke(null, startPosition.world, newTile)
                val tileMultipart: Class[_] = Class.forName("codechicken.multipart.TileMultipart")
                tileMultipart.getMethod("onMoved").invoke(newTile)
              }
              catch
                {
                  case e: Exception =>
                  {
                    e.printStackTrace
                  }
                }
            }
          }
          else
          {
            MovementUtility.setBlockSneaky(this.newPosition.world, this.newPosition, block, this.blockMetadata, null)
          }
          this.handler.queueEvent(new BlockNotifyDelayedEvent(this.handler, 0, startPosition.world, startPosition))
          this.handler.queueEvent(new BlockNotifyDelayedEvent(this.handler, 0, newPosition.world, newPosition))
          MinecraftForge.EVENT_BUS.post(new EventForceManipulate.EventPostForceManipulate(startPosition.world, startPosition.xi, startPosition.yi, startPosition.zi, newPosition.xi, newPosition.yi, newPosition.zi))
        }
        catch
          {
            case e: Exception =>
            {
              e.printStackTrace
            }
          }
      }
    }
  }

}
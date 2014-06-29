package mffs.field

import com.google.common.io.ByteArrayDataInput
import mffs.{MFFSHelper, ModularForceFieldSystem}
import mffs.tile.TileForceFieldProjector
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.api.mffs.IProjector
import resonant.lib.network.IPacketReceiver

class TileForceField extends TileAdvanced with IPacketReceiver
{
  def canUpdate: Boolean =
  {
    return false
  }

  def getDescriptionPacket: Nothing =
  {
    if (this.getProjector != null)
    {
      var itemID: Int = -1
      var itemMetadata: Int = -1
      if (camoStack != null)
      {
        itemID = camoStack.itemID
        itemMetadata = camoStack.getItemDamage
      }
      return ModularForceFieldSystem.PACKET_TILE.getPacket(this, this.projector.intX, this.projector.intY, this.projector.intZ, itemID, itemMetadata)
    }
    return null
  }

  def onReceivePacket(data: ByteArrayDataInput, player: EntityPlayer, obj: AnyRef*)
  {
    try
    {
      this.setProjector(new Nothing(data.readInt, data.readInt, data.readInt))
      this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord)
      this.camoStack = null
      val itemID: Int = data.readInt
      val itemMetadata: Int = data.readInt
      if (itemID != -1 && itemMetadata != -1)
      {
        this.camoStack = new ItemStack(Block.blocksList(itemID), 1, itemMetadata)
      }
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }
  }

  /**
   * @return Gets the projector block controlling this force field. Removes the force field if no
   *         projector can be found.
   */
  def getProjector: TileForceFieldProjector =
  {
    if (this.getProjectorSafe != null)
    {
      return getProjectorSafe
    }
    if (!this.worldObj.isRemote)
    {
      this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, 0)
    }
    return null
  }

  def setProjector(position: Nothing)
  {
    this.projector = position
    if (!this.worldObj.isRemote)
    {
      this.refreshCamoBlock
    }
  }

  def getProjectorSafe: TileForceFieldProjector =
  {
    if (this.projector != null)
    {
      if (this.projector.getTileEntity(this.worldObj).isInstanceOf[TileForceFieldProjector])
      {
        if (worldObj.isRemote || (projector.getTileEntity(this.worldObj).asInstanceOf[IProjector]).getCalculatedField.contains(new Nothing(this)))
        {
          return this.projector.getTileEntity(this.worldObj).asInstanceOf[TileForceFieldProjector]
        }
      }
    }
    return null
  }

  /**
   * Server Side Only
   */
  def refreshCamoBlock
  {
    if (this.getProjectorSafe != null)
    {
      this.camoStack = MFFSHelper.getCamoBlock(this.getProjector, new Nothing(this))
    }
  }

  def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.projector = new Nothing(nbt.getCompoundTag("projector"))
  }

  /**
   * Writes a tile entity to NBT.
   */
  def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    if (this.getProjector != null)
    {
      nbt.setCompoundTag("projector", this.projector.writeToNBT(new NBTTagCompound))
    }
  }

  var camoStack: ItemStack = null
  private var projector: Nothing = null
}
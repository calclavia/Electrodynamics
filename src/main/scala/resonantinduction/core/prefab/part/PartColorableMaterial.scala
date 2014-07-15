package resonantinduction.core.prefab.part

import java.util.ArrayList
import java.util.Collections
import java.util.List
import codechicken.multipart.TMultiPart
import net.minecraft.block.BlockColored
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemShears
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import codechicken.lib.data.MCDataInput
import codechicken.lib.data.MCDataOutput

/**
 * @author Calclavia
 *
 */
object PartColorableMaterial
{
  final val DEFAULT_COLOR: Int = 15
}

abstract class PartColorableMaterial[M](insulationType: Item) extends TMultiPart with TraitPart
{
  var color: Int = DEFAULT_COLOR
  var material: M = null
  var isInsulated: Boolean = false
  var requiresInsulation: Boolean = true

  /**
   * Material Methods
   */
  def getMaterial: M =
  {
    return material
  }

  def setMaterial(material: M)
  {
    this.material = material
  }

  def setMaterial(i: Int)

  def getMaterialID: Int =
  {
    return material.ordinal
  }

  /**
   * Insulation Methods
   */
  def setInsulated(insulated: Boolean)
  {
    this.isInsulated = insulated
    this.color = DEFAULT_COLOR
    if (!this.world.isRemote)
    {
      tile.notifyPartChange(this)
      this.sendInsulationUpdate
    }
  }

  def setInsulated(dyeColour: Int)
  {
    this.isInsulated = true
    this.color = dyeColour
    if (!this.world.isRemote)
    {
      tile.notifyPartChange(this)
      this.sendInsulationUpdate
      this.sendColorUpdate
    }
  }

  def insulated: Boolean =
  {
    return this.isInsulated
  }

  def sendInsulationUpdate
  {
    tile.getWriteStream(this).writeByte(1).writeBoolean(this.isInsulated)
  }

  /**
   * Wire Coloring Methods
   */
  def getColor: Int =
  {
    return if (isInsulated || !requiresInsulation) color else -1
  }

  def setColor(dye: Int)
  {
    if (isInsulated || !requiresInsulation)
    {
      this.color = dye
      if (!world.isRemote)
      {
        tile.notifyPartChange(this)
        onPartChanged(this)
        this.sendColorUpdate
      }
    }
  }

  def sendColorUpdate
  {
    tile.getWriteStream(this).writeByte(2).writeInt(this.color)
  }

  /**
   * Changes the wire's color.
   */
  override def activate(player: EntityPlayer, part: MovingObjectPosition, itemStack: ItemStack): Boolean =
  {
    if (itemStack != null)
    {
      val dyeColor: Int = MultipartUtility.isDye(itemStack)
      if (dyeColor != -1 && (isInsulated || !requiresInsulation))
      {
        if (!player.capabilities.isCreativeMode && requiresInsulation)
        {
          player.inventory.decrStackSize(player.inventory.currentItem, 1)
        }
        this.setColor(dyeColor)
        return true
      }
      else if (requiresInsulation)
      {
        if (itemStack.getItem eq insulationType)
        {
          if (this.isInsulated)
          {
            if (!world.isRemote && player.capabilities.isCreativeMode)
            {
              tile.dropItems(Collections.singletonList(new ItemStack(insulationType, 1, BlockColored.getBlockFromDye(color))))
            }
            this.setInsulated(false)
            return true
          }
          else
          {
            if (!player.capabilities.isCreativeMode)
            {
              player.inventory.decrStackSize(player.inventory.currentItem, 1)
            }
            this.setInsulated(BlockColored.getDyeFromBlock(itemStack.getItemDamage))
            return true
          }
        }
        else if (itemStack.getItem.isInstanceOf[ItemShears] && isInsulated)
        {
          if (!world.isRemote && !player.capabilities.isCreativeMode)
          {
            tile.dropItems(Collections.singletonList(new ItemStack(insulationType, 1, BlockColored.getBlockFromDye(color))))
          }
          this.setInsulated(false)
        }
        return true
      }
    }
    return false
  }

  override def getDrops: Iterable[ItemStack] =
  {
    val drops: List[ItemStack] = new ArrayList[ItemStack]
    drops.add(getItem)
    if (requiresInsulation && isInsulated)
    {
      drops.add(new ItemStack(insulationType, 1, BlockColored.getBlockFromDye(color)))
    }
    return drops
  }

  override def readDesc(packet: MCDataInput)
  {
    this.setMaterial(packet.readByte)
    this.color = packet.readByte
    this.isInsulated = packet.readBoolean
  }

  override def writeDesc(packet: MCDataOutput)
  {
    packet.writeByte(this.getMaterialID.asInstanceOf[Byte])
    packet.writeByte(this.color.asInstanceOf[Byte])
    packet.writeBoolean(this.isInsulated)
  }

  def read(packet: MCDataInput, packetID: Int)
  {
    packetID match
    {
      case 1 =>
        this.isInsulated = packet.readBoolean
        this.tile.markRender
        break //todo: break is not supported
      case 2 =>
        this.color = packet.readInt
        this.tile.markRender
        break //todo: break is not supported
    }
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setInteger("typeID", getMaterialID)
    nbt.setBoolean("isInsulated", isInsulated)
    nbt.setInteger("dyeID", color)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    setMaterial(nbt.getInteger("typeID"))
    this.isInsulated = nbt.getBoolean("isInsulated")
    this.color = nbt.getInteger("dyeID")
  }

}
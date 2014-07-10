package mffs.base

import io.netty.buffer.ByteBuf
import mffs.item.card.ItemCardLink
import mffs.{ModularForceFieldSystem, Reference}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import resonant.api.blocks.ICamouflageMaterial
import resonant.api.mffs.IActivatable
import resonant.content.spatial.block.SpatialTile
import resonant.lib.content.prefab.TRotatable
import resonant.lib.network.{IPacketReceiver, IPlayerUsing, PacketTile}
import resonant.lib.utility.inventory.InventoryUtility
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAll._

/**
 * A base tile class for all MFFS blocks to inherit.
 * @author Calclavia
 */
abstract class TileMFFS extends SpatialTile(Material.iron) with ICamouflageMaterial with IPacketReceiver with IActivatable with IPlayerUsing
{
  /**
   * Used for client side animations.
   */
  var animation = 0f
  /**
   * Is this machine switched on internally via GUI?
   */
  var isRedstoneActive = false
  /**
   * Is the machine active and working?
   */
  private var active = false

  /**
   * Constructor
   */
  blockHardness = Float.MaxValue
  blockResistance = 100f
  stepSound = Block.soundTypeMetal
  textureName = "machine"
  isOpaqueCube = false
  normalRender = false

  override protected def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!world.isRemote)
    {
      if (player.getCurrentEquippedItem != null)
      {
        if (player.getCurrentEquippedItem().getItem().isInstanceOf[ItemCardLink])
        {
          return false
        }
      }

      player.openGui(ModularForceFieldSystem, 0, world, x, y, z)
    }
    return true
  }

  override protected def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.isSneaking)
    {
      if (!world.isRemote)
      {
        InventoryUtility.dropBlockAsItem(world, position)
        world.setBlock(x, y, z, Blocks.air)
        return true
      }
      return false
    }

    if (this.isInstanceOf[TRotatable])
      return this.asInstanceOf[TRotatable].rotate(side, hit)

    return false
  }

  override def onNeighborChanged(block: Block)
  {
    if (!world.isRemote)
    {
      if (world.isBlockIndirectlyGettingPowered(x, y, z))
      {
        powerOn()
      }
      else
      {
        powerOff()
      }
    }
  }

  override def getExplosionResistance(entity: Entity): Float = 100

  override def update()
  {
    super.update()

    if (!world.isRemote && ticks % 3 == 0 && playersUsing.size > 0)
    {
      playersUsing foreach (player => ModularForceFieldSystem.packetHandler.sendToPlayer(getDescPacket, player.asInstanceOf[EntityPlayerMP]))
    }
  }

  /**
   * Override this for packet updating list.
   */
  def getPacketData(packetID: Int): List[AnyRef] =
  {
    if (packetID == TilePacketType.DESCRIPTION.id)
    {
      return List(TilePacketType.DESCRIPTION.id: Integer, active: java.lang.Boolean, isRedstoneActive: java.lang.Boolean)
    }

    return List[AnyRef]()
  }

  override def getDescriptionPacket: Packet =
  {
    return ModularForceFieldSystem.packetHandler.toMCPacket(getDescPacket)
  }

  def getDescPacket: PacketTile = new PacketTile(this, getPacketData(TilePacketType.DESCRIPTION.id).toArray: _*)

  override def onReceivePacket(data: ByteBuf, player: EntityPlayer, obj: AnyRef*)
  {
    try
    {
      onReceivePacket(data.readInt, data)
    }
    catch
      {
        case e: Exception =>
        {
          Reference.logger.error("Packet receiving failed: " + getClass.getSimpleName)
          e.printStackTrace
        }
      }
  }

  /**
   * Inherit this function to receive packets. Make sure this function is supered.
   *
   * @throws IOException
   */
  def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    if (packetID == TilePacketType.DESCRIPTION.id)
    {
      val prevActive = active
      active = dataStream.readBoolean()
      isRedstoneActive = dataStream.readBoolean()

      if (prevActive != this.active)
      {
        markRender()
      }
    }
    else if (packetID == TilePacketType.TOGGLE_ACTIVATION.id)
    {
      this.isRedstoneActive = !this.isRedstoneActive
      if (isRedstoneActive)
      {
        setActive(true)
      }
      else
      {
        setActive(false)
      }
    }
  }

  def isPoweredByRedstone: Boolean = world.isBlockIndirectlyGettingPowered(x, y, z)

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.active = nbt.getBoolean("isActive")
    this.isRedstoneActive = nbt.getBoolean("isRedstoneActive")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("isActive", this.active)
    nbt.setBoolean("isRedstoneActive", this.isRedstoneActive)
  }

  def isActive: Boolean = active

  def setActive(flag: Boolean)
  {
    this.active = flag
    this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
  }

  def powerOn()
  {
    this.setActive(true)
  }

  def powerOff()
  {
    if (!this.isRedstoneActive && !this.worldObj.isRemote)
    {
      this.setActive(false)
    }
  }

  /**
   * ComputerCraft

  def getType: String =
  {
    return this.getInvName
  }

  def getMethodNames: Array[String] =
  {
    return Array[String]("isActivate", "setActivate")
  }

  def callMethod(computer: Nothing, context: Nothing, method: Int, arguments: Array[AnyRef]): Array[AnyRef] =
  {
    method match
    {
      case 0 =>
      {
        return Array[AnyRef](this.isActive)
      }
      case 1 =>
      {
        this.setActive(arguments(0).asInstanceOf[Boolean])
        return null
      }
    }
    throw new Exception("Invalid method.")
  }

  def attach(computer: Nothing)
  {
  }

  def detach(computer: Nothing)
  {
  }*/
}

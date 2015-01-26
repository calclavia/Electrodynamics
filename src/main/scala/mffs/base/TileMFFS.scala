package mffs.base

import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.item.card.ItemCardLink
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import resonantengine.api.mffs.machine.IActivatable
import resonantengine.api.tile.{ICamouflageMaterial, IPlayerUsing}
import resonantengine.core.network.discriminator.PacketType
import resonantengine.core.network.netty.PacketManager
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.inventory.InventoryUtility
import resonantengine.lib.wrapper.ByteBufWrapper._
import resonantengine.prefab.block.traits.TRotatable
import resonantengine.prefab.network.{TPacketReceiver, TPacketSender}

import scala.collection.convert.wrapAll._

/**
 * A base tile class for all MFFS blocks to inherit.
 * @author Calclavia
 */
abstract class TileMFFS extends ResonantTile(Material.iron) with ICamouflageMaterial with TPacketReceiver with TPacketSender with IActivatable with IPlayerUsing
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

  override def onNeighborChanged(block: Block)
  {
    if (!world.isRemote)
    {
      if (world.isBlockIndirectlyGettingPowered(xi, yi, zi))
      {
        powerOn()
      }
      else
      {
        powerOff()
      }
    }
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

  override def getExplosionResistance(entity: Entity): Float = 100

  override def update()
  {
    super.update()

    if (!world.isRemote && ticks % 3 == 0 && playersUsing.size > 0)
    {
      playersUsing foreach (player => ModularForceFieldSystem.packetHandler.sendToPlayer(getDescPacket, player.asInstanceOf[EntityPlayerMP]))
    }
  }

  override def getDescriptionPacket: Packet =
  {
    return ModularForceFieldSystem.packetHandler.toMCPacket(getDescPacket)
  }

  override def getDescPacket: PacketType = PacketManager.request(this, TilePacketType.description.id)

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    if (id == TilePacketType.description.id)
    {
      val prevActive = active
      active = buf.readBoolean()
      isRedstoneActive = buf.readBoolean()

      if (prevActive != this.active)
      {
        markRender()
      }
    }
    else if (id == TilePacketType.toggleActivation.id)
    {
      isRedstoneActive = !isRedstoneActive

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

  def setActive(flag: Boolean)
  {
    active = flag
    worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
  }

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    if (id == TilePacketType.description.id)
    {
      buf <<< active
      buf <<< isRedstoneActive
    }
  }

  def isPoweredByRedstone: Boolean = world.isBlockIndirectlyGettingPowered(xi, yi, zi)

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

      player.openGui(ModularForceFieldSystem, 0, world, xi, yi, zi)
    }
    return true
  }

  override protected def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.isSneaking)
    {
      if (!world.isRemote)
      {
        InventoryUtility.dropBlockAsItem(world, toVector3)
        world.setBlock(xi, yi, zi, Blocks.air)
        return true
      }
      return false
    }

    if (this.isInstanceOf[TRotatable])
      return this.asInstanceOf[TRotatable].rotate(side, hit)

    return false
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

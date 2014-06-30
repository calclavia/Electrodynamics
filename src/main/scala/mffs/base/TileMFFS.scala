package mffs.base

import java.text.MessageFormat
import java.util

import com.google.common.io.ByteArrayDataInput
import mffs.ModularForceFieldSystem
import mffs.item.card.ItemCardLink
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import resonant.api.blocks.ICamouflageMaterial
import resonant.api.mffs.security.Permission
import resonant.api.mffs.{IActivatable, IBiometricIdentifierLink}
import resonant.api.{IPlayerUsing, IRotatable}
import resonant.content.spatial.block.SpatialTile
import resonant.lib.content.prefab.TRotatable
import resonant.lib.network.IPacketReceiver
import universalelectricity.core.transform.vector.Vector3

import scala.collection.mutable.HashSet

/**
 * A base tile class for all MFFS blocks to inherit.
 * @author Calclavia
 */
abstract class TileMFFS extends SpatialTile(Material.iron) with TRotatable with ICamouflageMaterial with IPacketReceiver with IPlayerUsing with TRotatable with IActivatable
{
  object TilePacketType extends Enumeration
  {
    val NONE, DESCRIPTION, FREQUENCY, FORTRON, TOGGLE_ACTIVATION, TOGGLE_MODE, INVENTORY, STRING, FXS, TOGGLE_MODE_2, TOGGLE_MODE_3, TOGGLE_MODE_4, FIELD, RENDER = Value
  }

  /**
   * The players to send packets to for machine update info.
   */
  final val playersUsing = new HashSet[EntityPlayer]()
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
  blockHardness = Float.MAX_VALUE
  blockResistance = 100f
  stepSound = Block.soundTypeMetal
  textureName = "machine"
  isOpaqueCube = false
  normalRender = false

  override protected def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!world.isRemote)
    {
      if (entityPlayer.getCurrentEquippedItem != null)
      {
        if (entityPlayer.getCurrentEquippedItem().getItem().isInstanceOf[ItemCardLink])
        {
          return false
        }
      }

      entityPlayer.openGui(ModularForceFieldSystem.instance, 0, world, x, y, z)
    }
    return true
  }

  override protected def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.isSneaking)
    {
      if (!world.isRemote)
      {
        val tileEntity: TileEntity = world.getTileEntity(x, y, z)
        if (tileEntity.isInstanceOf[IBiometricIdentifierLink])
        {
          if ((tileEntity.asInstanceOf[IBiometricIdentifierLink]).getBiometricIdentifier != null)
          {
            if ((tileEntity.asInstanceOf[IBiometricIdentifierLink]).getBiometricIdentifier.isAccessGranted(entityPlayer.username, Permission.SECURITY_CENTER_CONFIGURE))
            {
              this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0)
              world.setBlock(x, y, z, 0)
              return true
            }
            else
            {
              entityPlayer.addChatMessage("[" + ModularForceFieldSystem.blockBiometricIdentifier.getLocalizedName + "]" + " Cannot remove machine! Access denied!")
            }
          }
          else
          {
            this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0)
            world.setBlock(x, y, z, 0)
            return true
          }
        }
      }
      return false
    }

    return rotate(side, hit)
  }

  override def onNeighborChanged(block: Block)
  {
    if (!world.isRemote)
    {
      if (world.isBlockIndirectlyGettingPowered(x, y, z))
      {
        onPowerOn()
      }
      else
      {
        onPowerOff()
      }
    }
  }

  override def getExplosionResistance(entity: Entity, d: Double, d1: Double, d2: Double): Float = 100.0F

  override def update()
  {
    super.update()

    if (ticks % 3 == 0 && playersUsing.size() > 0)
    {
      playersUsing.foreach(ModularForceFieldSystem.packetHandler.sendToPlayer(getDescriptionPacket(), _))
    }
  }

  /**
   * Override this for packet updating list.
   */
  def getPacketData(packetID: Int): List[_] =
  {
    val data = List()
    if (packetID == TilePacketType.DESCRIPTION.ordinal)
    {
      data.add(TilePacketType.DESCRIPTION.ordinal)
      data.add(active)
      data.add(isRedstoneActive)
    }
    return data
  }

  override def getDescriptionPacket: Nothing =
  {
    return ModularForceFieldSystem.PACKET_TILE.getPacket(this, this.getPacketData(TilePacketType.DESCRIPTION.ordinal).toArray)
  }

  override def onReceivePacket(data: ByteArrayDataInput, player: EntityPlayer, obj: AnyRef*)
  {
    try
    {
      this.onReceivePacket(data.readInt, data)
    }
    catch
      {
        case e: Exception =>
        {
          ModularForceFieldSystem.LOGGER.severe(MessageFormat.format("Packet receiving failed: {0}", this.getClass.getSimpleName))
          e.printStackTrace
        }
      }
  }

  /**
   * Inherit this function to receive packets. Make sure this function is supered.
   *
   * @throws IOException
   */
  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    if (packetID == TilePacketType.DESCRIPTION.ordinal)
    {
      val prevActive: Boolean = this.active
      active = dataStream.readBoolean
      isRedstoneActive = dataStream.readBoolean
      if (prevActive != this.active)
      {
        this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord)
      }
    }
    else if (packetID == TilePacketType.TOGGLE_ACTIVATION.ordinal)
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

  def onPowerOn
  {
    this.setActive(true)
  }

  def onPowerOff
  {
    if (!this.isRedstoneActive && !this.worldObj.isRemote)
    {
      this.setActive(false)
    }
  }

  def getPlayersUsing: util.HashSet[EntityPlayer] =
  {
    return this.playersUsing
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

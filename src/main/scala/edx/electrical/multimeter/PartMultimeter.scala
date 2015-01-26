package edx.electrical.multimeter

import java.util.{ArrayList, HashSet, List, Set}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.vec
import codechicken.lib.vec.Cuboid6
import codechicken.multipart.{IRedstonePart, TMultiPart, TileMultipart}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Electrodynamics
import edx.core.interfaces.TNodeMechanical
import edx.core.prefab.part.ChickenBonesWrapper._
import edx.core.prefab.part.PartFace
import edx.electrical.ElectricalContent
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{ChatComponentText, MovingObjectPosition}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidTankInfo, IFluidHandler}
import resonantengine.api.tile.{INodeProvider, IRemovable}
import resonantengine.lib.mod.compat.energy.Compatibility
import resonantengine.lib.network.discriminator.PacketType
import resonantengine.lib.network.handle.IPacketReceiver
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.WrenchUtility

import scala.collection.convert.wrapAll._

/** Block that detects power.
  *
  * @author Calclavia
  */
class PartMultimeter extends PartFace with IRedstonePart with IPacketReceiver with IRemovable.ISneakWrenchable
{

  var playersUsing: Set[EntityPlayer] = new HashSet[EntityPlayer]
  /** Detection */
  var redstoneTriggerLimit: Double = .0
  var detectType: Byte = 0
  var graphType: Byte = 0
  var redstoneOn: Boolean = false
  var isPrimary: Boolean = false
  private var detectMode = DetectModes.NONE
  private var doDetect: Boolean = true
  private var grid: GridMultimeter = null

  override def preRemove()
  {
    if (!world.isRemote)
      getGrid.remove(this)
  }

  def updateDesc()
  {
    writeDesc(getWriteStream)
  }

  override def activate(player: EntityPlayer, part: MovingObjectPosition, item: ItemStack): Boolean =
  {
    if (WrenchUtility.isUsableWrench(player, player.inventory.getCurrentItem, x, y, z))
    {
      if (!this.world.isRemote)
      {
        doDetect = !doDetect
        player.addChatMessage(new ChatComponentText("Multimeter detection set to: " + doDetect))
        WrenchUtility.damageWrench(player, player.inventory.getCurrentItem, x, y, z)
      }
      return true
    }
    player.openGui(Electrodynamics, placementSide.ordinal, world, x, y, z)
    return true
  }

  override def update()
  {
    super.update()

    if (!world.isRemote)
    {
      if (doDetect) updateDetections
      val detectedValue = getGrid.graphs(detectType).getDouble()
      var outputRedstone = false

      detectMode match
      {
        case DetectModes.EQUAL =>
          outputRedstone = detectedValue == redstoneTriggerLimit
        case DetectModes.GREATER_THAN =>
          outputRedstone = detectedValue > redstoneTriggerLimit
        case DetectModes.GREATER_THAN_EQUAL =>
          outputRedstone = detectedValue >= redstoneTriggerLimit
        case DetectModes.LESS_THAN =>
          outputRedstone = detectedValue < redstoneTriggerLimit
        case DetectModes.LESS_THAN_EQUAL =>
          outputRedstone = detectedValue <= redstoneTriggerLimit
        case _ =>
      }

      getGrid.markUpdate
      if (ticks % 20 == 0)
      {
        if (outputRedstone != redstoneOn)
        {
          redstoneOn = outputRedstone
          tile.notifyPartChange(this)
        }
        updateGraph
      }
    }
    if (!world.isRemote)
    {
      for (player <- playersUsing)
      {
        updateGraph
      }
    }
  }

  def updateGraph()
  {
    sendPacket(2)
  }

  def updateDetections()
  {
    val receivingSide: ForgeDirection = getDirection.getOpposite
    val tileEntity: TileEntity = getDetectedTile

    if (tileEntity.isInstanceOf[INodeProvider])
    {
      val instance = ForgeDirection.values
        .map(dir => tileEntity.asInstanceOf[INodeProvider].getNode(classOf[TNodeMechanical], dir).asInstanceOf[TNodeMechanical])
        .headOption.orNull

      if (instance != null)
      {
        getGrid.torqueGraph.queue(instance.torque)
        getGrid.angularVelocityGraph.queue(instance.angularVelocity)
        getGrid.powerGraph.queue(instance.torque * instance.angularVelocity)
      }
    }
    if (tileEntity.isInstanceOf[IFluidHandler])
    {
      val fluidInfo: Array[FluidTankInfo] = tileEntity.asInstanceOf[IFluidHandler].getTankInfo(receivingSide)

      if (fluidInfo != null)
      {
        fluidInfo.filter(info => info != null && info.fluid != null).foreach(info => getGrid.fluidGraph.queue(info.fluid.amount))
      }
    }
    getGrid.energyGraph.queue(Compatibility.getEnergy(tileEntity, receivingSide))
  }

  def getDetectedTile: TileEntity =
  {
    val direction: ForgeDirection = getDirection
    return world.getTileEntity(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ)
  }

  override def write(packet: MCDataOutput, id: Int)
  {
    super.write(packet, id)

    id match
    {
      case 0 =>
      {
        packet.writeByte(placementSide.ordinal)
        packet.writeByte(facing)
        packet.writeByte(detectMode.id)
        packet.writeByte(detectType)
        packet.writeByte(graphType)
        packet.writeNBTTagCompound(getGrid.center.writeNBT(new NBTTagCompound))
        packet.writeNBTTagCompound(getGrid.size.writeNBT(new NBTTagCompound))
        packet.writeBoolean(getGrid.isEnabled)
      }
      case 2 =>
      {
        //Graph
        packet.writeByte(2)
        isPrimary = getGrid.isPrimary(this)
        packet.writeBoolean(isPrimary)
        if (isPrimary) packet.writeNBTTagCompound(getGrid.save)
      }
    }
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    packetID match
    {
      case 0 =>
      {
        placementSide = ForgeDirection.getOrientation(packet.readByte)
        facing = packet.readByte
        detectMode = DetectModes(packet.readByte).asInstanceOf[DetectModes.DetectMode]
        detectType = packet.readByte
        graphType = packet.readByte
        getGrid.center = new Vector3(packet.readNBTTagCompound)
        getGrid.size = new Vector3(packet.readNBTTagCompound)
        getGrid.isEnabled = packet.readBoolean
      }
      case 1 =>
      {
        redstoneTriggerLimit = packet.readLong
      }
      case 2 =>
      {
        isPrimary = packet.readBoolean
        if (isPrimary) getGrid.load(packet.readNBTTagCompound)
      }
    }
  }

  def getRemovedItems(entity: EntityPlayer): List[ItemStack] =
  {
    val list: List[ItemStack] = new ArrayList[ItemStack]
    list.add(new ItemStack(ElectricalContent.itemMultimeter))
    return list
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    detectMode = DetectModes(data.readByte).asInstanceOf[DetectModes.DetectMode]
    detectType = data.readByte
    graphType = data.readByte
    redstoneTriggerLimit = data.readDouble
  }

  def toggleGraphType
  {
    graphType = ((graphType + 1) % getGrid.graphs.size).asInstanceOf[Byte]
    updateServer
  }

  def toggleMode
  {
    detectMode = DetectModes((detectMode.id + 1) % DetectModes.values.size).asInstanceOf[DetectModes.DetectMode]
    updateServer
  }

  def updateServer
  {
  }

  def toggleDetectionValue
  {
    detectType = ((detectType + 1) % getGrid.graphs.size).asInstanceOf[Byte]
    updateServer
  }

  def getGrid: GridMultimeter =
  {
    if (grid == null)
    {
      grid = new GridMultimeter
      grid.add(this)
    }
    return grid
  }

  def setGrid(network: GridMultimeter)
  {
    grid = network
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    placementSide = ForgeDirection.getOrientation(nbt.getByte("side"))
    detectMode = DetectModes(nbt.getByte("detectMode")).asInstanceOf[DetectModes.DetectMode]
    detectType = nbt.getByte("detectionType")
    graphType = nbt.getByte("graphType")
    doDetect = nbt.getBoolean("doDetect")
    redstoneTriggerLimit = nbt.getDouble("triggerLimit")
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setByte("side", placementSide.ordinal.asInstanceOf[Byte])
    nbt.setByte("detectMode", detectMode.id.asInstanceOf[Byte])
    nbt.setByte("detectionType", detectType)
    nbt.setByte("graphType", graphType)
    nbt.setBoolean("doDetect", doDetect)
    nbt.setDouble("triggerLimit", redstoneTriggerLimit)
  }

  def getMode = detectMode

  override def redstoneConductionMap: Int =
  {
    return 0x1F
  }

  override def solid(arg0: Int): Boolean =
  {
    return true
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: vec.Vector3, frame: Float, pass: Int)
  {
    if (pass == 0)
    {
      RenderMultimeter.render(this, pos.x, pos.y, pos.z)
    }
  }

  def canConnectRedstone(arg0: Int): Boolean =
  {
    return true
  }

  def strongPowerLevel(arg0: Int): Int =
  {
    return if (redstoneOn) 14 else 0
  }

  def weakPowerLevel(arg0: Int): Int =
  {
    return if (redstoneOn) 14 else 0
  }

  def canConnect(direction: ForgeDirection, obj: AnyRef): Boolean =
  {
    return obj.isInstanceOf[PartMultimeter]
  }

  def getConnections: Array[AnyRef] =
  {
    val connections: Array[AnyRef] = new Array[AnyRef](6)
    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      if (dir != getDirection && dir != getDirection.getOpposite)
      {
        val vector: Vector3 = getPosition.add(dir)
        if (hasMultimeter(vector.xi, vector.yi, vector.zi))
        {
          connections(dir.ordinal) = getMultimeter(vector.xi, vector.yi, vector.zi)
        }
      }
    }
    return connections
  }

  def getDirection: ForgeDirection =
  {
    return ForgeDirection.getOrientation(this.placementSide.ordinal)
  }

  def hasMultimeter(x: Int, y: Int, z: Int): Boolean =
  {
    return getMultimeter(x, y, z) != null
  }

  /** Gets the multimeter on the same plane. */
  def getMultimeter(x: Int, y: Int, z: Int): PartMultimeter =
  {
    val tileEntity: TileEntity = world.getTileEntity(x, y, z)

    if (tileEntity.isInstanceOf[TileMultipart])
    {
      val part: TMultiPart = (tileEntity.asInstanceOf[TileMultipart]).partMap(placementSide.ordinal)
      if (part.isInstanceOf[PartMultimeter])
      {
        return part.asInstanceOf[PartMultimeter]
      }
    }

    return null
  }

  def getPosition: Vector3 =
  {
    return new Vector3(x, y, z)
  }

  @SideOnly(Side.CLIENT)
  override def getRenderBounds: Cuboid6 =
  {
    if (isPrimary) return Cuboid6.full.copy.expand(new Vector3(getGrid.size.x, getGrid.size.y, getGrid.size.z))
    return Cuboid6.full
  }

  override def toString: String = "[PartMultimeter]" + x + "x " + y + "y " + z + "z " + getSlotMask + "s "

  protected def getItem: ItemStack =
  {
    return new ItemStack(ElectricalContent.itemMultimeter)
  }

  object DetectModes extends Enumeration
  {

    val NONE = DetectMode("none")
    val LESS_THAN = DetectMode("lessThan")
    val LESS_THAN_EQUAL = DetectMode("lessThanOrEqual")
    val EQUAL = DetectMode("equal")
    val GREATER_THAN_EQUAL = DetectMode("greaterThanOrEqual")
    val GREATER_THAN = DetectMode("greaterThan")

    case class DetectMode(display: String) extends super.Val(nextId)

  }

}
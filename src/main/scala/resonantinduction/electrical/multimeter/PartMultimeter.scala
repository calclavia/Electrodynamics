package resonantinduction.electrical.multimeter

import java.util.{ArrayList, HashSet, List, Set}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.vec
import codechicken.lib.vec.Cuboid6
import codechicken.multipart.{IRedstonePart, TMultiPart, TileMultipart}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{ChatComponentText, MovingObjectPosition}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidTankInfo, IFluidHandler}
import resonant.api.IRemovable
import resonant.api.grid.INodeProvider
import resonant.lib.grid.Compatibility
import resonant.engine.network.discriminator.PacketType
import resonant.engine.network.handle.IPacketReceiver
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.WrenchUtility
import resonantinduction.core.ResonantInduction
import resonantinduction.core.interfaces.TNodeMechanical
import resonantinduction.core.prefab.part.ChickenBonesWrapper._
import resonantinduction.core.prefab.part.PartFace
import resonantinduction.electrical.ElectricalContent

import scala.collection.convert.wrapAll._

/** Block that detects power.
  *
  * @author Calclavia
  */
class PartMultimeter extends PartFace with IRedstonePart with IPacketReceiver with IRemovable.ISneakWrenchable
{

  object DetectModes extends Enumeration
  {

    case class DetectMode(display: String) extends super.Val(nextId)

    val NONE = DetectMode("none")
    val LESS_THAN = DetectMode("lessThan")
    val LESS_THAN_EQUAL = DetectMode("lessThanOrEqual")
    val EQUAL = DetectMode("equal")
    val GREATER_THAN_EQUAL = DetectMode("greaterThanOrEqual")
    val GREATER_THAN = DetectMode("greaterThan")
  }

  var playersUsing: Set[EntityPlayer] = new HashSet[EntityPlayer]
  /** Detection */
  var redstoneTriggerLimit: Double = .0
  var detectType: Byte = 0
  var graphType: Byte = 0
  private var detectMode = DetectModes.NONE
  var redstoneOn: Boolean = false
  private var doDetect: Boolean = true
  var isPrimary: Boolean = false
  private var network: MultimeterGrid = null

  def hasMultimeter(x: Int, y: Int, z: Int): Boolean =
  {
    return getMultimeter(x, y, z) != null
  }

  override def preRemove
  {
    if (!world.isRemote)
      getNetwork.remove(this)
  }

  def updateDesc
  {
    writeDesc(getWriteStream)
  }

  def updateGraph()
  {
    sendPacket(2)
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
    player.openGui(ResonantInduction, placementSide.ordinal, world, x, y, z)
    return true
  }

  override def update()
  {
    super.update()

    if (!world.isRemote)
    {
      if (doDetect) updateDetections
      val detectedValue = getNetwork.graphs(detectType).getDouble
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

      getNetwork.markUpdate
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
        getNetwork.torqueGraph.queue(instance.torque)
        getNetwork.angularVelocityGraph.queue(instance.angularVelocity)
        getNetwork.powerGraph.queue(instance.torque * instance.angularVelocity)
      }
    }
    if (tileEntity.isInstanceOf[IFluidHandler])
    {
      val fluidInfo: Array[FluidTankInfo] = tileEntity.asInstanceOf[IFluidHandler].getTankInfo(receivingSide)

      if (fluidInfo != null)
      {
        fluidInfo.filter(info => info != null && info.fluid != null).foreach(info => getNetwork.fluidGraph.queue(info.fluid.amount))
      }
    }
    getNetwork.energyGraph.queue(Compatibility.getEnergy(tileEntity, receivingSide))
    getNetwork.energyCapacityGraph.queue(Compatibility.getMaxEnergy(tileEntity, receivingSide))
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
        packet.writeNBTTagCompound(getNetwork.center.writeNBT(new NBTTagCompound))
        packet.writeNBTTagCompound(getNetwork.size.writeNBT(new NBTTagCompound))
        packet.writeBoolean(getNetwork.isEnabled)
      }
      case 2 =>
      {
        //Graph
        packet.writeByte(2)
        isPrimary = getNetwork.isPrimary(this)
        packet.writeBoolean(isPrimary)
        if (isPrimary) packet.writeNBTTagCompound(getNetwork.save)
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
        getNetwork.center = new Vector3(packet.readNBTTagCompound)
        getNetwork.size = new Vector3(packet.readNBTTagCompound)
        getNetwork.isEnabled = packet.readBoolean
      }
      case 1 =>
      {
        redstoneTriggerLimit = packet.readLong
      }
      case 2 =>
      {
        isPrimary = packet.readBoolean
        if (isPrimary) getNetwork.load(packet.readNBTTagCompound)
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

  def getDetectedTile: TileEntity =
  {
    val direction: ForgeDirection = getDirection
    return world.getTileEntity(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ)
  }

  def getDirection: ForgeDirection =
  {
    return ForgeDirection.getOrientation(this.placementSide.ordinal)
  }

  def toggleGraphType
  {
    graphType = ((graphType + 1) % getNetwork.graphs.size).asInstanceOf[Byte]
    updateServer
  }

  def toggleMode
  {
    detectMode = DetectModes((detectMode.id + 1) % DetectModes.values.size).asInstanceOf[DetectModes.DetectMode]
    updateServer
  }

  def toggleDetectionValue
  {
    detectType = ((detectType + 1) % getNetwork.graphs.size).asInstanceOf[Byte]
    updateServer
  }

  def updateServer
  {
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

  protected def getItem: ItemStack =
  {
    return new ItemStack(ElectricalContent.itemMultimeter)
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

  def getNetwork: MultimeterGrid =
  {
    if (network == null)
    {
      network = new MultimeterGrid
      network.add(this)
    }
    return network
  }

  def setNetwork(network: MultimeterGrid)
  {
    this.network = network
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

  def getPosition: Vector3 =
  {
    return new Vector3(x, y, z)
  }

  @SideOnly(Side.CLIENT)
  override def getRenderBounds: Cuboid6 =
  {
    if (isPrimary) return Cuboid6.full.copy.expand(new Vector3(getNetwork.size.x, getNetwork.size.y, getNetwork.size.z))
    return Cuboid6.full
  }

  override def toString: String = "[PartMultimeter]" + x + "x " + y + "y " + z + "z " + getSlotMask + "s "
}
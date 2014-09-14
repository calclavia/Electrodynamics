package resonantinduction.core.prefab.part.connector

import java.util
import java.util.{Collection, HashSet, Set}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.vec.Cuboid6
import codechicken.multipart._
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{IIcon, MovingObjectPosition}
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.api.core.grid.{INode, INodeProvider, ISave}

object PartFramedNode
{
  var sides: Array[IndexedCuboid6] = new Array[IndexedCuboid6](7)
  var insulatedSides: Array[IndexedCuboid6] = new Array[IndexedCuboid6](7)

  sides(0) = new IndexedCuboid6(0, new Cuboid6(0.36, 0.000, 0.36, 0.64, 0.36, 0.64))
  sides(1) = new IndexedCuboid6(1, new Cuboid6(0.36, 0.64, 0.36, 0.64, 1.000, 0.64))
  sides(2) = new IndexedCuboid6(2, new Cuboid6(0.36, 0.36, 0.000, 0.64, 0.64, 0.36))
  sides(3) = new IndexedCuboid6(3, new Cuboid6(0.36, 0.36, 0.64, 0.64, 0.64, 1.000))
  sides(4) = new IndexedCuboid6(4, new Cuboid6(0.000, 0.36, 0.36, 0.36, 0.64, 0.64))
  sides(5) = new IndexedCuboid6(5, new Cuboid6(0.64, 0.36, 0.36, 1.000, 0.64, 0.64))
  sides(6) = new IndexedCuboid6(6, new Cuboid6(0.36, 0.36, 0.36, 0.64, 0.64, 0.64))
  insulatedSides(0) = new IndexedCuboid6(0, new Cuboid6(0.3, 0.0, 0.3, 0.7, 0.3, 0.7))
  insulatedSides(1) = new IndexedCuboid6(1, new Cuboid6(0.3, 0.7, 0.3, 0.7, 1.0, 0.7))
  insulatedSides(2) = new IndexedCuboid6(2, new Cuboid6(0.3, 0.3, 0.0, 0.7, 0.7, 0.3))
  insulatedSides(3) = new IndexedCuboid6(3, new Cuboid6(0.3, 0.3, 0.7, 0.7, 0.7, 1.0))
  insulatedSides(4) = new IndexedCuboid6(4, new Cuboid6(0.0, 0.3, 0.3, 0.3, 0.7, 0.7))
  insulatedSides(5) = new IndexedCuboid6(5, new Cuboid6(0.7, 0.3, 0.3, 1.0, 0.7, 0.7))
  insulatedSides(6) = new IndexedCuboid6(6, new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7))

  def connectionMapContainsSide(connections: Byte, side: ForgeDirection): Boolean =
  {
    val tester: Byte = (1 << side.ordinal).asInstanceOf[Byte]
    return ((connections & tester) > 0)
  }
}

abstract class PartFramedNode extends TMultiPart with INodeProvider with TSlottedPart with TNormalOcclusion with TIconHitEffects
{
  /** Bitmask connections */
  var connectionMask: Byte = 0x00
  protected var connections: Array[AnyRef] = new Array[AnyRef](6)
  protected var node: INode = null

  @SideOnly(Side.CLIENT)
  protected var breakIcon: IIcon = null

  /** Client Side */
  private var testingSide: ForgeDirection = null

  override def occlusionTest(other: TMultiPart): Boolean =
  {
    return NormalOcclusionTest.apply(this, other)
  }

  override def getStrength(hit: MovingObjectPosition, player: EntityPlayer): Float =
  {
    return 10F
  }

  def getBounds: Cuboid6 =
  {
    return new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625)
  }

  override def getBreakingIcon(subPart: Any, side: Int): IIcon =
  {
    return breakIcon
  }

  def getBrokenIcon(side: Int): IIcon =
  {
    return breakIcon
  }

  def getOcclusionBoxes: Set[Cuboid6] =
  {
    return getCollisionBoxes
  }

  /** Rendering and block bounds. */
  override def getCollisionBoxes: Set[Cuboid6] =
  {
    val collisionBoxes: Set[Cuboid6] = new HashSet[Cuboid6]
    collisionBoxes.addAll(getSubParts.asInstanceOf[Collection[_ <: Cuboid6]])
    return collisionBoxes
  }

  override def getSubParts: java.lang.Iterable[IndexedCuboid6] =
  {
    super.getSubParts

    val currentSides: Array[IndexedCuboid6] = if (this.isInstanceOf[TInsulatable] && this.asInstanceOf[TInsulatable].insulated) PartFramedNode.insulatedSides.clone() else PartFramedNode.sides.clone()

    val list = new util.LinkedList[IndexedCuboid6]

    if (tile != null)
    {
      for (side <- ForgeDirection.VALID_DIRECTIONS)
      {
        if (PartFramedNode.connectionMapContainsSide(getAllCurrentConnections, side) || side == testingSide) list.add(currentSides(side.ordinal()))
      }
    }
    return list
  }

  def getAllCurrentConnections: Byte =
  {
    return (connectionMask)
  }

  def getSlotMask: Int =
  {
    return PartMap.CENTER.mask
  }

  def getHollowSize: Int =
  {
    return if (this.isInstanceOf[TInsulatable] && this.asInstanceOf[TInsulatable].insulated) 8 else 6
  }

  def isBlockedOnSide(side: ForgeDirection): Boolean =
  {
    val blocker: TMultiPart = tile.partMap(side.ordinal)
    testingSide = side
    val expandable: Boolean = NormalOcclusionTest.apply(this, blocker)
    testingSide = null
    return !expandable
  }

  override def bind(t: TileMultipart)
  {
    node.deconstruct
    super.bind(t)
    node.reconstruct
  }

  def isCurrentlyConnected(side: ForgeDirection): Boolean =
  {
    return PartFramedNode.connectionMapContainsSide(getAllCurrentConnections, side)
  }

  override def onWorldJoin()
  {
    node.reconstruct()
  }

  override def onNeighborChanged()
  {
    node.reconstruct()
  }

  override def onWorldSeparate()
  {
    node.deconstruct()
  }

  /** Packet Methods */
  def sendConnectionUpdate()
  {
    tile.getWriteStream(this).writeByte(0).writeByte(connectionMask)
  }

  override def readDesc(packet: MCDataInput)
  {
    super.readDesc(packet)
    connectionMask = packet.readByte
  }

  override def writeDesc(packet: MCDataOutput)
  {
    super.writeDesc(packet)
    packet.writeByte(connectionMask)
  }

  override def read(packet: MCDataInput)
  {
    read(packet, packet.readUByte)
  }

  def read(packet: MCDataInput, packetID: Int)
  {
    if (packetID == 0)
    {
      connectionMask = packet.readByte
      tile.markRender
    }
  }

  def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
  {
    if (node != null && nodeType != null)
    {
      return node
    }
    return null
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)

    if (node.isInstanceOf[ISave])
      node.asInstanceOf[ISave].save(nbt)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)

    if (node.isInstanceOf[ISave])
      node.asInstanceOf[ISave].load(nbt)
  }

  def getNode: INode = node

  def setNode(n: INode)
  { node = n }
}
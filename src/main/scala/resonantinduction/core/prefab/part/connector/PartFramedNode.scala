package resonantinduction.core.prefab.part.connector

import java.lang.{Iterable => JIterable}
import java.util.Set

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.vec.Cuboid6
import codechicken.multipart._
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.{IIcon, MovingObjectPosition}
import net.minecraftforge.common.util.ForgeDirection

import scala.collection.convert.wrapAll._
import scala.collection.mutable

object PartFramedNode
{
  var sides = new Array[IndexedCuboid6](7)
  var insulatedSides = new Array[IndexedCuboid6](7)

  val center: IndexedCuboid6 = new IndexedCuboid6(7, new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625))

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

  def connectionMapContainsSide(connections: Int, side: ForgeDirection): Boolean =
  {
    val tester = 1 << side.ordinal
    return (connections & tester) > 0
  }
}

abstract class PartFramedNode extends PartAbstract with TNodePartConnector with TSlottedPart with TNormalOcclusion with TIconHitEffects
{
  /** Bitmask connections */
  var connectionMask = 0x00

  @SideOnly(Side.CLIENT)
  protected var breakIcon: IIcon = null

  /** Client Side */
  private var testingSide: ForgeDirection = null

  override def getStrength(hit: MovingObjectPosition, player: EntityPlayer): Float = 10f

  override def getBounds: Cuboid6 = PartFramedNode.center

  override def getBrokenIcon(side: Int): IIcon = breakIcon

  def getOcclusionBoxes: Set[Cuboid6] = getCollisionBoxes

  /** Rendering and block bounds. */
  override def getCollisionBoxes: Set[Cuboid6] =
  {
    val collisionBoxes = mutable.Set.empty[Cuboid6]
    collisionBoxes ++= getSubParts
    return collisionBoxes
  }

  override def getSubParts: JIterable[IndexedCuboid6] =
  {
    val currentSides = if (this.isInstanceOf[TInsulatable] && this.asInstanceOf[TInsulatable].insulated) PartFramedNode.insulatedSides else PartFramedNode.sides

    val list = mutable.Set.empty[IndexedCuboid6]
    list += PartFramedNode.center
    list ++= ForgeDirection.VALID_DIRECTIONS.filter(s => PartFramedNode.connectionMapContainsSide(connectionMask, s) || s == testingSide).map(s => currentSides(s.ordinal()))
    return list
  }

  override def getSlotMask = PartMap.CENTER.mask

  def isBlockedOnSide(side: ForgeDirection): Boolean =
  {
    val blocker: TMultiPart = tile.partMap(side.ordinal)
    testingSide = side
    val expandable = NormalOcclusionTest.apply(this, blocker)
    testingSide = null
    return !expandable
  }

  def isCurrentlyConnected(side: ForgeDirection): Boolean = PartFramedNode.connectionMapContainsSide(connectionMask, side)

  /** Packet Methods */
  def sendConnectionUpdate()
  {
    if (!world.isRemote)
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

  override def read(packet: MCDataInput, packetID: Int)
  {
    super.read(packet, packetID)

    if (packetID == 0)
    {
      connectionMask = packet.readByte
      tile.markRender()
    }
  }
}
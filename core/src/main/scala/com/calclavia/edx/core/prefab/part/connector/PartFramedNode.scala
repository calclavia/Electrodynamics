package com.calclavia.edx.core.prefab.part.connector

import java.lang.{Iterable => JIterable}
import java.util.Set

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.vec.Cuboid6
import codechicken.multipart._
import com.calclavia.edx.core.prefab.part.CuboidShapes
import CuboidShapes
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovingObjectPosition
import nova.core.util.Direction
import resonantengine.lib.grid.core.NodeConnector
import resonantengine.lib.wrapper.BitmaskWrapper._

import scala.collection.convert.wrapAll._
import scala.collection.mutable

abstract class PartFramedNode extends PartAbstract with TPartNodeProvider with TSlottedPart with TNormalOcclusion
{
  protected val node: NodeConnector[_]
  /** Bitmask connections */
  var clientRenderMask = 0x00

  /** Client Side */
  protected var testingSide: Direction = null

  //Check if lazy val will be null?
  nodes.add(node)

  override def getStrength(hit: MovingObjectPosition, player: EntityPlayer): Float = 10f

  def getOcclusionBoxes: Set[Cuboid6] = getCollisionBoxes

  /** Rendering and block bounds. */
  override def getCollisionBoxes: Set[Cuboid6] = mutable.Set.empty[Cuboid6] ++ getSubParts

  override def getSubParts: JIterable[IndexedCuboid6] =
  {
    val sideCuboids = if (this.isInstanceOf[TInsulatable] && this.asInstanceOf[TInsulatable].insulated) CuboidShapes.thickSegment else CuboidShapes.segment
    val list = mutable.Set.empty[IndexedCuboid6]
    list += CuboidShapes.center
    list ++= Direction.VALID_DIRECTIONS.filter(s => clientRenderMask.mask(s) || s == testingSide).map(s => sideCuboids(s.ordinal()))
    return list
  }

  override def getSlotMask = PartMap.CENTER.mask

  def isBlockedOnSide(side: Direction): Boolean =
  {
    val blocker: TMultiPart = tile.partMap(side.ordinal)
    testingSide = side
    val expandable = NormalOcclusionTest.apply(this, blocker)
    testingSide = null
    return !expandable
  }

  def isCurrentlyConnected(side: Direction): Boolean = clientRenderMask.mask(side)

  override def write(packet: MCDataOutput, id: Int)
  {
    super.write(packet, id)

    if (id == 0)
    {
      packet.writeByte(node.connectedMask.toByte)
    }
  }

  override def read(packet: MCDataInput, id: Int)
  {
    super.read(packet, id)

    if (id == 0)
    {
      clientRenderMask = packet.readByte()
    }
  }

  @deprecated
  def connectionMapContainsSide(connections: Int, side: Direction): Boolean =
  {
    val tester = 1 << side.ordinal
    return (connections & tester) > 0
  }
}
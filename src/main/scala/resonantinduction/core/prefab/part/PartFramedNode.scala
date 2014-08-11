package resonantinduction.core.prefab.part

import java.util
import java.util.{Collection, HashSet, Set}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.vec.Cuboid6
import codechicken.multipart.{IconHitEffects, JIconHitEffects, JNormalOcclusion, NormalOcclusionTest, PartMap, TMultiPart, TSlottedPart, TileMultipart}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.particle.EffectRenderer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{IIcon, MovingObjectPosition}
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.api.core.grid.{INode, INodeProvider}

object PartFramedNode {
  def connectionMapContainsSide(connections: Byte, side: ForgeDirection): Boolean = {
    val tester: Byte = (1 << side.ordinal).asInstanceOf[Byte]
    return ((connections & tester) > 0)
  }

  var sides: Array[IndexedCuboid6] = new Array[IndexedCuboid6](7)
  var insulatedSides: Array[IndexedCuboid6] = new Array[IndexedCuboid6](7)
}

abstract class PartFramedNode[M](insulationType: Item) extends PartColorableMaterial[M](insulationType: Item) with INodeProvider with TSlottedPart with JNormalOcclusion with JIconHitEffects {

  protected var connections: Array[AnyRef] = new Array[AnyRef](6)
  protected var node: INode
  /** Bitmask connections */
  var currentConnections: Byte = 0x00
  /** Client Side */
  private var testingSide: ForgeDirection = null
  @SideOnly(Side.CLIENT) protected var breakIcon: IIcon = null

  def preparePlacement(meta: Int) {
    this.setMaterial(meta)
  }

  override def occlusionTest(other: TMultiPart): Boolean = {
    return NormalOcclusionTest.apply(this, other)
  }

  override def getSubParts: java.lang.Iterable[IndexedCuboid6] = {
    super.getSubParts
    val currentSides: Array[IndexedCuboid6] = if (isInsulated) PartFramedNode.insulatedSides.clone() else PartFramedNode.sides.clone()
    val list : util.LinkedList[IndexedCuboid6]  = new util.LinkedList[IndexedCuboid6]
    if (tile != null)
    {
      for (side <- ForgeDirection.VALID_DIRECTIONS)
      {
        if (PartFramedNode.connectionMapContainsSide(getAllCurrentConnections, side) || side == testingSide) list.add(currentSides(side.ordinal()))
      }
    }
    return list
  }

  /** Rendering and block bounds. */
  override def getCollisionBoxes: Set[Cuboid6] = {
    val collisionBoxes: Set[Cuboid6] = new HashSet[Cuboid6]
    collisionBoxes.addAll(getSubParts.asInstanceOf[Collection[_ <: Cuboid6]])
    return collisionBoxes;
  }

  override def getStrength(hit: MovingObjectPosition, player: EntityPlayer): Float = {
    return 10F
  }

  def getBounds: Cuboid6 = {
    return new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625)
  }

  override def getBreakingIcon(subPart: Any, side: Int): IIcon = {
    return breakIcon
  }

  def getBrokenIcon(side: Int): IIcon = {
    return breakIcon
  }

  def getOcclusionBoxes: Set[Cuboid6] = {
    return getCollisionBoxes
  }

  def getSlotMask: Int = {
    return PartMap.CENTER.mask
  }

  def getHollowSize: Int = {
    return if (isInsulated) 8 else 6
  }

  override def addHitEffects(hit: MovingObjectPosition, effectRenderer: EffectRenderer) {
    IconHitEffects.addHitEffects(this, hit, effectRenderer)
  }

  override def addDestroyEffects(effectRenderer: EffectRenderer) {
    IconHitEffects.addDestroyEffects(this, effectRenderer, false)
  }

  def isBlockedOnSide(side: ForgeDirection): Boolean = {
    val blocker: TMultiPart = tile.partMap(side.ordinal)
    testingSide = side
    val expandable: Boolean = NormalOcclusionTest.apply(this, blocker)
    testingSide = null
    return !expandable
  }

  def getAllCurrentConnections: Byte = {
    return (currentConnections)
  }

  override def bind(t: TileMultipart) {
    node.deconstruct
    super.bind(t)
    node.reconstruct
  }

  def isCurrentlyConnected(side: ForgeDirection): Boolean = {
    return PartFramedNode.connectionMapContainsSide(getAllCurrentConnections, side)
  }

  override def onWorldJoin {
    node.reconstruct
  }

  override def onNeighborChanged {
    node.reconstruct
  }

  override def onWorldSeparate {
    node.deconstruct
  }

  def copyFrom(other: PartFramedNode[M]) {
    this.isInsulated = other.isInsulated
    this.color = other.color
    this.connections = other.connections
    this.material = other.material
  }

  /** Packet Methods */
  def sendConnectionUpdate {
    tile.getWriteStream(this).writeByte(0).writeByte(currentConnections)
  }

  override def readDesc(packet: MCDataInput) {
    super.readDesc(packet)
    currentConnections = packet.readByte
  }

  override def writeDesc(packet: MCDataOutput) {
    super.writeDesc(packet)
    packet.writeByte(currentConnections)
  }

  override def read(packet: MCDataInput) {
    read(packet, packet.readUByte)
  }

  override def read(packet: MCDataInput, packetID: Int) {
    if (packetID == 0) {
      currentConnections = packet.readByte
      tile.markRender
    }
    else {
      super.read(packet, packetID)
    }
  }

  @SuppressWarnings(Array("hiding")) def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode = {
    if (node != null && nodeType != null) {
        return node
    }
    return null
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    node.save(nbt)
  }

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    node.load(nbt)
  }

  override def toString: String = {
    return this.getClass.getSimpleName + this.hashCode
  }
}
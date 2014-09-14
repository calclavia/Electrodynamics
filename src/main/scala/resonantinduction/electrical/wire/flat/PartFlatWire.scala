package resonantinduction.electrical.wire.flat

import java.lang.{Iterable => JIterable}

import codechicken.lib.colour.{Colour, ColourARGB}
import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.render.{CCRenderState, TextureUtils}
import codechicken.lib.vec.{BlockCoord, Cuboid6, Rotation, Vector3}
import codechicken.multipart._
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemDye
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{IIcon, MovingObjectPosition}
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonantinduction.core.prefab.part.ChickenBonesWrapper._
import resonantinduction.core.util.MultipartUtil
import resonantinduction.electrical.wire.base.TWire
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.simulator.dc.DCNode

import scala.collection.convert.wrapAll._

/**
 * This is the class for all flat wire types. It can be used for any sub type, as it contains the
 * base calculations necessary to create a working wire. This calculates all possible connections to
 * sides, around corners, and inside corners, while checking for microblock obstructions.
 *
 * @author Calclavia, MrTJP
 */
object PartFlatWire
{
  var selectionBounds = Array.ofDim[Cuboid6](3, 6)
  var occlusionBounds = Array.ofDim[Cuboid6](3, 6)

  for (t <- 0 until 3)
  {
    lazy val selection: Cuboid6 = new Cuboid6(0, 0, 0, 1, (t + 2) / 16D, 1).expand(-0.005)
    lazy val occlusion: Cuboid6 = new Cuboid6(2 / 8D, 0, 2 / 8D, 6 / 8D, (t + 2) / 16D, 6 / 8D)
    {
      for (s <- 0 until 6)
      {
        selectionBounds(t)(s) = selection.copy.apply(Rotation.sideRotations(s).at(Vector3.center))
        occlusionBounds(t)(s) = occlusion.copy.apply(Rotation.sideRotations(s).at(Vector3.center))
      }
    }
  }
}

class PartFlatWire extends TWire with TFacePart with TNormalOcclusion
{
  /**
   * The current side the wire is placed on
   */
  var side: Byte = 0

  /**
   * A map of the corners.
   * <p/>
   * <p/>
   * Currently split into 4 nybbles (from lowest) 0 = Corner connections (this wire should connect
   * around a corner to something external) 1 = Straight connections (this wire should connect to
   * something external) 2 = Internal connections (this wire should connect to something internal)
   * 3 = Internal open connections (this wire is not blocked by a cover/edge part and *could*
   * connect through side) bit 16 = connection to the centerpart 5 = Render corner connections.
   * Like corner connections but set to low if the other wire part is smaller than this (they
   * render to us not us to them)
   */
  var connMap: Int = 0x00

  override lazy val node = new FlatWireNode(this)

  def preparePlacement(side: Int, meta: Int)
  {
    this.side = (side ^ 1).toByte
    setMaterial(meta)
    node.setResistance(material.resistance)
  }

  def canStay: Boolean =
  {
    val pos: BlockCoord = new BlockCoord(tile).offset(side)
    return MultipartUtil.canPlaceWireOnSide(world, pos.x, pos.y, pos.z, ForgeDirection.getOrientation(side ^ 1), false)
  }

  def dropIfCantStay: Boolean =
  {
    if (!canStay)
    {
      drop
      return true
    }
    return false
  }

  def drop
  {
    TileMultipart.dropItem(getItem, world, Vector3.fromTileEntityCenter(tile))
    tile.remPart(this)
  }

  def renderThisCorner(part: PartFlatWire): Boolean =
  {
    if (!(part.isInstanceOf[PartFlatWire]))
    {
      return false
    }
    val wire: PartFlatWire = part
    if (wire.getThickness == getThickness)
    {
      return side < wire.side
    }
    return wire.getThickness > getThickness
  }

  /**
   * Packet Methods
   */
  override def load(tag: NBTTagCompound)
  {
    super.load(tag)
    this.side = tag.getByte("side")
    this.connMap = tag.getInteger("connMap")
  }

  override def save(tag: NBTTagCompound)
  {
    super.save(tag)
    tag.setByte("side", this.side)
    tag.setInteger("connMap", this.connMap)
  }

  override def readDesc(packet: MCDataInput)
  {
    super.readDesc(packet)
    side = packet.readByte
    connMap = packet.readInt
  }

  override def writeDesc(packet: MCDataOutput)
  {
    super.writeDesc(packet)
    packet.writeByte(side)
    packet.writeInt(connMap)
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    super.read(packet, packetID)

    if (packetID == 0)
    {
      connMap = packet.readInt
      tile.markRender
    }
  }

  def sendConnUpdate()
  {
    tile.getWriteStream(this).writeByte(0).writeInt(this.connMap)
  }

  /**
   * Events
   */
  override def onRemoved()
  {
    super.onRemoved()

    if (!world.isRemote)
    {
      for (r <- 0 until 4)
      {
        if (maskConnects(r))
        {
          if ((connMap & 1 << r) != 0)
            notifyCornerChange(r)
          else if ((connMap & 0x10 << r) != 0)
            notifyStraightChange(r)
        }
      }
    }
  }

  override def onChunkLoad()
  {
    if ((connMap & 0x80000000) != 0)
    {
      if (dropIfCantStay)
        return

      connMap = 0
      tile.markDirty()
    }

    super.onChunkLoad()
  }

  override def onAdded
  {
    super.onAdded()

    if (!world.isRemote)
    {
      sendConnUpdate()
    }
  }

  override def onPartChanged(part: TMultiPart)
  {
    if (!world.isRemote)
    {
      sendConnUpdate()
    }

    super.onPartChanged(part)
  }

  override def onNeighborChanged
  {
    if (!world.isRemote)
      if (dropIfCantStay)
        return

    super.onNeighborChanged()
  }

  def notifyCornerChange(r: Int)
  {
    val absDir: Int = Rotation.rotateSide(side, r)
    val pos: BlockCoord = new BlockCoord(tile).offset(absDir).offset(side)
    world.notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, tile.getBlockType)
  }

  def notifyStraightChange(r: Int)
  {
    val absDir: Int = Rotation.rotateSide(side, r)
    val pos: BlockCoord = new BlockCoord(tile).offset(absDir)
    world.notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, tile.getBlockType)
  }

  def maskConnects(r: Int): Boolean =
  {
    return (connMap & 0x111 << r) != 0
  }

  def maskOpen(r: Int): Boolean =
  {
    return (connMap & 0x1000 << r) != 0
  }

  /**
   * START TILEMULTIPART INTERACTIONS *
   */
  override def getStrength(hit: MovingObjectPosition, player: EntityPlayer): Float =
  {
    return 4
  }

  def getSlotMask: Int =
  {
    return 1 << this.side
  }

  override def getSubParts: JIterable[IndexedCuboid6] = Seq(new IndexedCuboid6(0, PartFlatWire.selectionBounds(getThickness)(side)))

  def getOcclusionBoxes: JIterable[Cuboid6] = Seq(PartFlatWire.occlusionBounds(getThickness)(side))

  def getThickness: Int =
  {
    return if (insulated) 2 else 1
  }

  override def solid(arg0: Int) = false

  /**
   * Rendering
   */
  @SideOnly(Side.CLIENT)
  def getIcon: IIcon =
  {
    return RenderFlatWire.flatWireTexture
  }

  def getColour: Colour =
  {
    if (insulated)
    {
      val color: Colour = new ColourARGB(ItemDye.field_150922_c(colorID))
      color.a = 255.asInstanceOf[Byte]
      return color
    }

    return new ColourARGB(material.color)
  }

  def useStaticRenderer: Boolean = true

  @SideOnly(Side.CLIENT)
  override def renderStatic(pos: Vector3, pass: Int): Boolean =
  {
    if (pass == 0 && useStaticRenderer)
    {
      CCRenderState.setBrightness(world, x, y, z)
      RenderFlatWire.render(this, pos)
      CCRenderState.setColour(-1)
      return true
    }
    return false
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    if (pass == 0 && !useStaticRenderer)
    {
      GL11.glDisable(GL11.GL_LIGHTING)
      TextureUtils.bindAtlas(0)
      CCRenderState.startDrawing(7)
      RenderFlatWire.render(this, pos)
      CCRenderState.draw
      CCRenderState.setColour(-1)
      GL11.glEnable(GL11.GL_LIGHTING)
    }
  }

  @SideOnly(Side.CLIENT)
  override def drawBreaking(renderBlocks: RenderBlocks)
  {
    CCRenderState.reset()
    RenderFlatWire.renderBreakingOverlay(renderBlocks.overrideBlockTexture, this)
  }

  /**
   * Flat wire node handles all the connection logic
   * TODO: ForgeDirection may NOT be suitable. Integers are better.
   * @param provider
   */
  class FlatWireNode(provider: INodeProvider) extends DCNode(provider)
  {
    override def buildConnections()
    {
      updateOpenConnections()

      /**
       * 6 bit bitmask to mark sides that are already calculated to determine which side to disconnect.
       * TODO: Check if the bitshifting is correct
       * E.g: 000010
       */
      var calculatedMask = 0x00

      for (r <- 0 until 4)
      {
        if (maskOpen(r))
        {
          var skip = false
          val absDir: Int = Rotation.rotateSide(side, r)

          if (setExternalConnection(r, absDir))
            calculatedMask = calculatedMask | (1 << absDir)

          val cornerPos: BlockCoord = new BlockCoord(tile)
          cornerPos.offset(absDir)

          if (canConnectThroughCorner(cornerPos, absDir ^ 1, side))
          {
            cornerPos.offset(side)

            val tpCorner: TileMultipart = MultipartUtil.getMultipartTile(world, cornerPos)

            if (tpCorner != null)
            {
              val tp: TMultiPart = tpCorner.partMap(absDir ^ 1)
              val absForgeDir = ForgeDirection.getOrientation(absDir)

              if (canConnectTo(tp, absForgeDir))
              {
                connections.put(tp, absForgeDir)

                calculatedMask = calculatedMask | (1 << absDir)
                skip = true
              }
            }
          }

          if ((calculatedMask & (1 << absDir)) != 0 && !skip)
          {
            disconnect(absDir)
          }
        }
      }

      for (r <- 0 until 4)
      {
        var skip = false

        val absDir: Int = Rotation.rotateSide(side, r)

        if (tile.partMap(PartMap.edgeBetween(absDir, side)) == null)
        {
          val tp: TMultiPart = tile.partMap(absDir)

          if (canConnectTo(tp))
          {
            connections.put(tp, ForgeDirection.getOrientation(absDir))
            skip = true
          }
        }

        if ((calculatedMask & (1 << absDir)) != 0 && !skip)
        {
          disconnect(absDir)
        }
      }

      setExternalConnection(-1, side)
    }

    def setExternalConnection(r: Int, absSide: Int): Boolean =
    {
      val pos: BlockCoord = new BlockCoord(tile).offset(absSide)
      val tileMultiPart: TileMultipart = MultipartUtil.getMultipartTile(world, pos)
      if (tileMultiPart != null && r != -1)
      {
        val tp: TMultiPart = tileMultiPart.partMap(side)

        if (canConnectTo(tp, ForgeDirection.getOrientation(absSide)))
        {
          val otherR = (r + 2) % 4
          val forgeDir = ForgeDirection.getOrientation(absSide)

          //Check if it's another flat wire.
          if (tp.isInstanceOf[PartFlatWire] && (tp.asInstanceOf[PartFlatWire]).canConnectTo(this, ForgeDirection.getOrientation(absSide).getOpposite) && tp.asInstanceOf[PartFlatWire].maskOpen(otherR))
          {
            connections.put(tp, forgeDir)
            return true
          }

          //Check if it's a component.
          if (canConnectTo(tp))
          {
            connections.put(tp, forgeDir)
            return true
          }
        }

        disconnect(absSide)
      }

      val tileEntity = world.getTileEntity(pos.x, pos.y, pos.z)
      val forgeDir = ForgeDirection.getOrientation(absSide)

      if (canConnectTo(tileEntity, forgeDir))
      {
        connections.put(tileEntity, forgeDir)
        return true
      }

      disconnect(absSide)
      return false
    }

    private def disconnect(i: Int)
    {
      if (!world.isRemote)
      {
        //TODO: Refine this. It's very hacky and may cause errors when the wire connects to a block both ways
        val inverseCon = connections.map(_.swap)
        val forgeDir = ForgeDirection.getOrientation(i)
        val connected = inverseCon(forgeDir)

        if (connected != null)
        {
          connections -= connected
        }
      }
    }

    /**
     * Recalculates connections to blocks outside this space
     *
     * @return true if a new connection was added or one was removed
     */
    protected def updateExternalConnections(): Boolean =
    {
      var newConn: Int = 0

      for (r <- 0 until 4)
      {
        if (maskOpen(r))
        {

          if (connectStraight(r))
          {
            newConn |= 0x10 << r
          }
          else
          {
            val cnrMode: Int = connectCorner(r)
            if (cnrMode != 0)
            {
              newConn |= 1 << r
              if (cnrMode == 2)
              {
                newConn |= 0x100000 << r
              }
            }
          }
        }
      }

      if (newConn != (connMap & 0xF000FF))
      {
        val diff: Int = connMap ^ newConn
        connMap = connMap & ~0xF000FF | newConn

        for (r <- 0 until 4)
        {
          if ((diff & 1 << r) != 0)
          {
            notifyCornerChange(r)
          }
        }

        return true
      }
      return false
    }

    /**
     * Recalculates connections to other parts within this space
     *
     * @return true if a new connection was added or one was removed
     */
    protected def updateInternalConnections(): Boolean =
    {
      var newConn: Int = 0

      for (r <- 0 until 4)
      {
        if (connectInternal(r))
        {
          newConn |= 0x100 << r
        }
      }

      if (connectCenter)
      {
        newConn |= 0x10000
      }
      if (newConn != (connMap & 0x10F00))
      {
        connMap = connMap & ~0x10F00 | newConn
        return true
      }
      return false
    }

    /**
     * Recalculates connections that can be made to other parts outside of this space
     *
     * @return true if external connections should be recalculated
     */
    protected def updateOpenConnections(): Boolean =
    {
      var newConn: Int = 0

      for (r <- 0 until 4)
      {
        if (connectionOpen(r))
        {
          newConn |= 0x1000 << r
        }
      }

      if (newConn != (connMap & 0xF000))
      {
        connMap = connMap & ~0xF000 | newConn
        return true
      }

      return false
    }

    def connectionOpen(r: Int): Boolean =
    {
      val absDir: Int = Rotation.rotateSide(side, r)
      val facePart: TMultiPart = tile.partMap(absDir)
      if (facePart != null && (!(facePart.isInstanceOf[PartFlatWire]) || !canConnectTo(facePart, ForgeDirection.getOrientation(absDir))))
      {
        return false
      }
      if (tile.partMap(PartMap.edgeBetween(side, absDir)) != null)
      {
        return false
      }
      return true
    }

    /**
     * Return a corner connection state. 0 = No connection 1 = Physical connection 2 = Render
     * connection
     */
    def connectCorner(r: Int): Int =
    {
      val absDir: Int = Rotation.rotateSide(side, r)
      val pos: BlockCoord = new BlockCoord(tile)
      pos.offset(absDir)
      if (!canConnectThroughCorner(pos, absDir ^ 1, side))
      {
        return 0
      }
      pos.offset(side)
      val t: TileMultipart = MultipartUtil.getMultipartTile(world, pos)

      if (t != null)
      {
        val tp = t.partMap(absDir ^ 1)

        if (canConnectTo(tp, ForgeDirection.getOrientation(absDir)))
        {
          val wire = getWireNode(tp, ForgeDirection.getOrientation(absDir))

          if (wire != null)
          {
            if (wire.connectCorner(PartFlatWire.this, Rotation.rotationTo(absDir ^ 1, side ^ 1)))
            {
              if (!renderThisCorner(tp.asInstanceOf[PartFlatWire]))
              {
                return 1
              }
              return 2
            }
          }
          return 2
        }
      }
      return 0
    }

    def canConnectThroughCorner(pos: BlockCoord, side1: Int, side2: Int): Boolean =
    {
      if (world.isAirBlock(pos.x, pos.y, pos.z))
      {
        return true
      }
      val t: TileMultipart = MultipartUtil.getMultipartTile(world, pos)
      if (t != null)
      {
        return t.partMap(side1) == null && t.partMap(side2) == null && t.partMap(PartMap.edgeBetween(side1, side2)) == null
      }
      return false
    }

    def connectStraight(r: Int): Boolean =
    {
      val absDir: Int = Rotation.rotateSide(side, r)
      val pos: BlockCoord = new BlockCoord(tile).offset(absDir)
      val t: TileMultipart = MultipartUtil.getMultipartTile(world, pos)
      if (t != null)
      {
        val tp: TMultiPart = t.partMap(side)
        if (canConnectTo(tp, ForgeDirection.getOrientation(absDir)))
        {
          val wire = getWireNode(tp, ForgeDirection.getOrientation(absDir))

          if (wire != null)
          {
            return wire.connectStraight(PartFlatWire.this, (r + 2) % 4)
          }
          return true
        }
      }
      else
      {
        val tileEntity: TileEntity = world.getTileEntity(pos.x, pos.y, pos.z)
        return canConnectTo(tileEntity, ForgeDirection.getOrientation(absDir))
      }
      return false
    }

    def connectInternal(r: Int): Boolean =
    {
      val absDir: Int = Rotation.rotateSide(side, r)
      if (tile.partMap(PartMap.edgeBetween(absDir, side)) != null)
      {
        return false
      }
      val tp: TMultiPart = tile.partMap(absDir)

      if (canConnectTo(tp, ForgeDirection.getOrientation(absDir)))
      {
        val wire = getWireNode(tp, ForgeDirection.getOrientation(absDir))
        if (wire != null)
          return wire.connectInternal(PartFlatWire.this, Rotation.rotationTo(absDir, side))
      }

      return false
    }

    def connectCenter: Boolean =
    {
      val tp: TMultiPart = tile.partMap(6)

      if (canConnectTo(tp))
      {
        if (tp.isInstanceOf[PartFlatWire])
          return tp.asInstanceOf[PartFlatWire].getNode(classOf[FlatWireNode], null).asInstanceOf[FlatWireNode].connectInternal(PartFlatWire.this, side)

        return true
      }

      return false
    }

    def connectCorner(wire: PartFlatWire, r: Int): Boolean =
    {
      val absDir: Int = Rotation.rotateSide(side, r)
      if (canConnectTo(wire, ForgeDirection.getOrientation(absDir)) && maskOpen(r))
      {
        val oldConn: Int = connMap
        connMap |= 0x1 << r
        if (renderThisCorner(wire))
        {
          connMap |= 0x100000 << r
        }
        if (oldConn != connMap)
        {
          sendConnUpdate
        }
        return true
      }
      return false
    }

    def connectStraight(wire: PartFlatWire, r: Int): Boolean =
    {
      val absDir: Int = Rotation.rotateSide(side, r)
      if (canConnectTo(wire, ForgeDirection.getOrientation(absDir)) && maskOpen(r))
      {
        val oldConn: Int = connMap
        connMap |= 0x10 << r
        if (oldConn != connMap)
        {
          sendConnUpdate
        }
        return true
      }
      return false
    }

    def connectInternal(wire: PartFlatWire, r: Int): Boolean =
    {
      val absDir: Int = Rotation.rotateSide(side, r)
      if (canConnectTo(wire, ForgeDirection.getOrientation(absDir)))
      {
        val oldConn: Int = connMap
        connMap |= 0x100 << r
        if (oldConn != connMap)
        {
          sendConnUpdate
        }
        return true
      }
      return false
    }

    private def getWireNode(obj: AnyRef, from: ForgeDirection): FlatWireNode =
    {
      if (obj.isInstanceOf[FlatWireNode])
        return obj.asInstanceOf[FlatWireNode]

      if (obj.isInstanceOf[INodeProvider])
        return getWireNode(obj.asInstanceOf[INodeProvider].getNode(classOf[FlatWireNode], from), from)

      return null
    }
  }

}
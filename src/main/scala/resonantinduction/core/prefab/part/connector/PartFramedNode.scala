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
import resonantinduction.core.prefab.part.CuboidShapes

import scala.collection.convert.wrapAll._
import scala.collection.mutable


abstract class PartFramedNode extends PartAbstract with TPartNodeProvider with TSlottedPart with TNormalOcclusion with TIconHitEffects
{
    /** Bitmask connections */
    protected var clientRenderMask = 0x00

    @SideOnly(Side.CLIENT)
    protected var breakIcon: IIcon = null

    /** Client Side */
    private var testingSide: ForgeDirection = null

    override def getStrength(hit: MovingObjectPosition, player: EntityPlayer): Float = 10f

    override def getBounds: Cuboid6 = CuboidShapes.WIRE_CENTER

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
        val currentSides = if (this.isInstanceOf[TInsulatable] && this.asInstanceOf[TInsulatable].insulated) CuboidShapes.WIRE_INSULATION else CuboidShapes.WIRE_SEGMENTS

        val list = mutable.Set.empty[IndexedCuboid6]
        list += CuboidShapes.WIRE_CENTER
        list ++= ForgeDirection.VALID_DIRECTIONS.filter(s => connectionMapContainsSide(clientRenderMask, s) || s == testingSide).map(s => currentSides(s.ordinal()))
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

    def isCurrentlyConnected(side: ForgeDirection): Boolean = connectionMapContainsSide(clientRenderMask, side)

    /** Packet Methods */
    def sendConnectionUpdate()
    {
        if (!world.isRemote)
            tile.getWriteStream(this).writeByte(0).writeByte(node)
    }

    override def readDesc(packet: MCDataInput)
    {
        super.readDesc(packet)
        clientRenderMask = packet.readByte
    }

    override def writeDesc(packet: MCDataOutput)
    {
        super.writeDesc(packet)
        packet.writeByte(clientRenderMask)
    }

    override def read(packet: MCDataInput, packetID: Int)
    {
        super.read(packet, packetID)

        if (packetID == 0)
        {
            clientRenderMask = packet.readByte
            tile.markRender()
        }
    }

    def connectionMapContainsSide(connections: Int, side: ForgeDirection): Boolean =
    {
        val tester = 1 << side.ordinal
        return (connections & tester) > 0
    }
}
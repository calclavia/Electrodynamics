package resonantinduction.mechanical.mech

import java.util.{ArrayList, List}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.multipart._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.util.ForgeDirection
import resonant.engine.ResonantEngine
import resonantinduction.core.prefab.part.connector.PartAbstract
import universalelectricity.api.core.grid.{INode, INodeProvider}
import universalelectricity.core.transform.vector.VectorWorld

/** We assume all the force acting on the gear is 90 degrees.
  *
  * @author Calclavia */
abstract class PartMechanical extends PartAbstract with JNormalOcclusion with TFacePart with INodeProvider
{
    /** Node that handles resonantinduction.mechanical action of the machine */
    var node: MechanicalNode = null
    protected var prevAngularVelocity: Double = .0
    /** Packets */
    private[mech] var markPacketUpdate: Boolean = false
    /** Simple debug external GUI */
    private[mech] var frame: DebugFrameMechanical = null
    /** Side of the block this is placed on */
    var placementSide: ForgeDirection = ForgeDirection.UNKNOWN
    var tier: Int = 0

    def preparePlacement(side: Int, itemDamage: Int)
    {
        this.placementSide = ForgeDirection.getOrientation((side).asInstanceOf[Byte])
        this.tier = itemDamage
    }

    override def onNeighborChanged
    {
        super.onNeighborChanged
        node.reconstruct
    }

    override def onPartChanged(part: TMultiPart)
    {
        super.onPartChanged(part)
        if (part.isInstanceOf[INodeProvider])
        {
            node.reconstruct
        }
    }

    override def update
    {
        this.node.update
        if (!world.isRemote)
        {
            checkClientUpdate
        }
        if (frame != null)
        {
            frame.update
        }
        super.update
    }

    override def activate(player: EntityPlayer, hit: MovingObjectPosition, itemStack: ItemStack): Boolean =
    {
        if (ResonantEngine.runningAsDev)
        {
            if (itemStack != null && !world.isRemote)
            {
                if (itemStack.getItem eq Items.stick)
                {
                    if (ControlKeyModifer.isControlDown(player))
                    {
                        if (frame == null)
                        {
                            frame = new DebugFrameMechanical(this)
                            frame.showDebugFrame
                        }
                        else
                        {
                            frame.closeDebugFrame
                            frame = null
                        }
                    }
                }
            }
        }
        return super.activate(player, hit, itemStack)
    }

    def checkClientUpdate
    {
        if (Math.abs(prevAngularVelocity - node.angularVelocity) >= 0.1)
        {
            prevAngularVelocity = node.angularVelocity
            sendRotationPacket
        }
    }

    def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
    {
        if (nodeType.isAssignableFrom(node.getClass))
        {
            return node
        }
        return null
    }

    override def onWorldJoin
    {
        node.reconstruct
    }

    override def onWorldSeparate
    {
        node.deconstruct
        if (frame != null)
        {
            frame.closeDebugFrame
        }
    }

    /** Packet Code. */
    def sendRotationPacket
    {
        if (world != null && !world.isRemote)
        {
            getWriteStream.writeByte(1).writeDouble(node.angularVelocity)
        }
    }

    /** Packet Code. */
    override def read(packet: MCDataInput)
    {
        read(packet, packet.readUByte)
    }

    override def read(packet: MCDataInput, packetID: Int)
    {
        if (packetID == 0)
        {
            load(packet.readNBTTagCompound)
        }
        else if (packetID == 1)
        {
            node.angularVelocity = packet.readDouble
        }
    }

    override def readDesc(packet: MCDataInput)
    {
        packet.readByte
        load(packet.readNBTTagCompound)
    }

    override def writeDesc(packet: MCDataOutput)
    {
        packet.writeByte(0)
        val nbt: NBTTagCompound = new NBTTagCompound
        save(nbt)
        packet.writeNBTTagCompound(nbt)
    }

    override def redstoneConductionMap: Int =
    {
        return 0
    }

    override def solid(arg0: Int): Boolean =
    {
        return true
    }

    override def load(nbt: NBTTagCompound)
    {
        placementSide = ForgeDirection.getOrientation(nbt.getByte("side"))
        tier = nbt.getByte("tier")
        node.load(nbt)
    }

    override def save(nbt: NBTTagCompound)
    {
        nbt.setByte("side", placementSide.ordinal.asInstanceOf[Byte])
        nbt.setByte("tier", tier.asInstanceOf[Byte])
        node.save(nbt)
    }

    protected def getItem: ItemStack

    override def getDrops: java.lang.Iterable[ItemStack] =
    {
        val drops: List[ItemStack] = new ArrayList[ItemStack]
        drops.add(getItem)
        return drops
    }

    override def pickItem(hit: MovingObjectPosition): ItemStack =
    {
        return getItem
    }

    def getPosition: VectorWorld =
    {
        return new VectorWorld(world, x, y, z)
    }

    override def toString: String =
    {
        return "[" + getClass.getSimpleName + "]" + x + "x " + y + "y " + z + "z " + getSlotMask + "s "
    }
}
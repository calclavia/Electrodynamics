package resonantinduction.mechanical.mech

import codechicken.multipart.ControlKeyModifer
import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraftforge.common.util.ForgeDirection
import resonant.content.prefab.java.TileNode
import resonant.engine.ResonantEngine
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketIDReceiver
import universalelectricity.api.core.grid.{INode, INodeProvider}
import universalelectricity.core.transform.vector.Vector3

/** Prefab for resonantinduction.mechanical tiles
  *
  * @author Calclavia */
object TileMechanical
{
    protected final val PACKET_NBT: Int = 0
    protected final val PACKET_VELOCITY: Int = 1
}

abstract class TileMechanical(material: Material) extends TileNode(material) with INodeProvider with IPacketIDReceiver
{
    /** Node that handles most mechanical actions */
    var mechanicalNode: MechanicalNode = null
    /** External debug GUI */
    private[mech] var frame: DebugFrameMechanical = null

    this.mechanicalNode = new MechanicalNode(this)

    override def update
    {
        super.update
        if (frame != null)
        {
            frame.update
            if (!frame.isVisible)
            {
                frame.dispose
                frame = null
            }
        }
        if (!this.getWorldObj.isRemote)
        {
            if (ticks % 3 == 0 && (mechanicalNode.markTorqueUpdate || mechanicalNode.markRotationUpdate))
            {
                sendRotationPacket
                mechanicalNode.markRotationUpdate = false
                mechanicalNode.markTorqueUpdate = false
            }
        }
    }

    override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
    {
        val itemStack: ItemStack = player.getHeldItem
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
        return false
    }

    override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
    {
        return mechanicalNode
    }

    override def getDescriptionPacket: Packet =
    {
        val tag: NBTTagCompound = new NBTTagCompound
        writeToNBT(tag)
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(x, y, z, Array(TileMechanical.PACKET_NBT, tag)))
    }

    /** Sends the torque and angular velocity to the client */
    private def sendRotationPacket
    {
        ResonantEngine.instance.packetHandler.sendToAllAround(new PacketTile(x, y , z, Array(TileMechanical.PACKET_VELOCITY, mechanicalNode.angularVelocity, mechanicalNode.torque)), this)
    }

    override def read(data: ByteBuf, id: Int, player: EntityPlayer, `type`: PacketType): Boolean =
    {
        if (world.isRemote)
        {
            if (id == TileMechanical.PACKET_NBT)
            {
                readFromNBT(ByteBufUtils.readTag(data))
                return true
            }
            else if (id == TileMechanical.PACKET_VELOCITY)
            {
                mechanicalNode.angularVelocity = data.readDouble
                mechanicalNode.torque = data.readDouble
                return true
            }
        }
        return false
    }

    override def readFromNBT(nbt: NBTTagCompound)
    {
        super.readFromNBT(nbt)
        mechanicalNode.load(nbt)
    }

    override def writeToNBT(nbt: NBTTagCompound)
    {
        super.writeToNBT(nbt)
        mechanicalNode.save(nbt)
    }
}
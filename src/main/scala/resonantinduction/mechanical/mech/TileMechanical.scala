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
import resonant.api.grid.{INodeProvider, INode}
import resonant.lib.transform.vector.Vector3

/** Prefab for resonantinduction.mechanical tiles
  *
  * @author Calclavia */
abstract class TileMechanical(material: Material) extends TileNode(material) with INodeProvider with IPacketIDReceiver
{
    /** Internal packet ID for NBTTagCompound parsing from packets */
    protected var nbt_packet_id: Int = 0
    /** Internal packet ID for rotation and velocity */
    protected var vel_packet_id: Int = 1

    /** Node that handles most mechanical actions */
    var mechanicalNode: MechanicalNode = new MechanicalNode(this)
    /** External debug GUI */
    var frame: DebugFrameMechanical = null


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
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(xi, yi, zi, Array(nbt_packet_id, tag)))
    }

    /** Sends the torque and angular velocity to the client */
    private def sendRotationPacket
    {
        ResonantEngine.instance.packetHandler.sendToAllAround(new PacketTile(xi, yi , zi, Array(vel_packet_id, mechanicalNode.angularVelocity, mechanicalNode.torque)), this)
    }

    override def read(data: ByteBuf, id: Int, player: EntityPlayer, `type`: PacketType): Boolean =
    {
        if (world.isRemote)
        {
            if (id == nbt_packet_id)
            {
                readFromNBT(ByteBufUtils.readTag(data))
                return true
            }
            else if (id == vel_packet_id)
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
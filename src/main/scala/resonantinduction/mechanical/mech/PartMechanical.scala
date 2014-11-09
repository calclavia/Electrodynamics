package resonantinduction.mechanical.mech

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.multipart._
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.transform.vector.{Vector3, VectorWorld}
import resonantinduction.core.prefab.part.connector.{PartAbstract, TPartNodeProvider}
import resonantinduction.mechanical.mech.grid.MechanicalNode

/** We assume all the force acting on the gear is 90 degrees.
  *
  * @author Calclavia */
abstract class PartMechanical extends PartAbstract with JNormalOcclusion with TFacePart with TPartNodeProvider with TCuboidPart
{
  /** Node that handles resonantinduction.mechanical action of the machine */
  private var _mechanicalNode: MechanicalNode = null

  def mechanicalNode = _mechanicalNode

  def mechanicalNode_=(mech: MechanicalNode)
  {
    _mechanicalNode = mech
    mechanicalNode.onVelocityChanged = () => sendVelocityPacket()
    nodes.add(mechanicalNode)
  }

  /** Side of the block this is placed on */
  var placementSide: ForgeDirection = ForgeDirection.UNKNOWN

  /** The tier of this mechanical part */
  var tier = 0

  def preparePlacement(side: Int, itemDamage: Int)
  {
    this.placementSide = ForgeDirection.getOrientation((side).asInstanceOf[Byte])
    this.tier = itemDamage
  }

  /** Packet Code. */
  def sendVelocityPacket()
  {
    if (world != null && !world.isRemote)
    {
      getWriteStream.writeByte(1).writeDouble(mechanicalNode.angularVelocity)
    }
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    if (packetID == 0)
    {
      load(packet.readNBTTagCompound)
    }
    else if (packetID == 1)
    {
      mechanicalNode.angularVelocity = packet.readDouble
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

  override def redstoneConductionMap: Int = 0

  override def solid(arg0: Int): Boolean = true

  override def load(nbt: NBTTagCompound)
  {
    placementSide = ForgeDirection.getOrientation(nbt.getByte("side"))
    tier = nbt.getByte("tier")
  }

  override def save(nbt: NBTTagCompound)
  {
    nbt.setByte("side", placementSide.ordinal.asInstanceOf[Byte])
    nbt.setByte("tier", tier.asInstanceOf[Byte])
  }

  def getPosition: VectorWorld = new VectorWorld(world, x, y, z)

  override def toString: String = "[" + getClass.getSimpleName + "]" + x + "x " + y + "y " + z + "z " + getSlotMask + "s "
}
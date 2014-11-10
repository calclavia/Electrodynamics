package resonantinduction.mechanical.mech

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.multipart._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.transform.vector.VectorWorld
import resonantinduction.core.prefab.part.connector.{PartAbstract, TPartNodeProvider}
import resonantinduction.mechanical.mech.grid.MechanicalNode

/** We assume all the force acting on the gear is 90 degrees.
  *
  * @author Calclavia */
abstract class PartMechanical extends PartAbstract with JNormalOcclusion with TFacePart with TPartNodeProvider with TCuboidPart
{
  /** Node that handles resonantinduction.mechanical action of the machine */
  private var _mechanicalNode: MechanicalNode = null

  protected var markVelocityUpdate = false

  def mechanicalNode = _mechanicalNode

  def mechanicalNode_=(mech: MechanicalNode)
  {
    _mechanicalNode = mech
    mechanicalNode.onVelocityChanged = () => markVelocityUpdate = true
    nodes.add(mechanicalNode)
  }

  /** Side of the block this is placed on */
  var placementSide: ForgeDirection = ForgeDirection.UNKNOWN

  /** The tier of this mechanical part */
  var tier = 0

  override def update()
  {
    super.update()

    if (markVelocityUpdate)
    {
      sendPacket(1)
      markVelocityUpdate = false
    }
  }

  def preparePlacement(side: Int, itemDamage: Int)
  {
    this.placementSide = ForgeDirection.getOrientation((side).asInstanceOf[Byte])
    this.tier = itemDamage
  }

  override def activate(player: EntityPlayer, hit: MovingObjectPosition, item: ItemStack): Boolean =
  {
    if (!world.isRemote)
    {
      println("Angle: " + mechanicalNode.prevAngle)
      sendPacket(2)
    }

    super.activate(player, hit, item)
  }

  override def write(packet: MCDataOutput, id: Int)
  {
    super.write(packet, id)

    id match
    {
      case 0 =>
        packet.writeByte(placementSide.ordinal.toByte)
        packet.writeByte(tier.toByte)
      case 1 => packet.writeFloat(mechanicalNode.angularVelocity.toFloat)
      case 2 => packet.writeFloat(mechanicalNode.prevAngle.toFloat)
    }
  }

  override def read(packet: MCDataInput, id: Int)
  {
    super.read(packet, id)

    id match
    {
      case 0 =>
        placementSide = ForgeDirection.getOrientation(packet.readByte())
        tier = packet.readByte()
      case 1 => mechanicalNode.angularVelocity = packet.readFloat()
      case 2 => mechanicalNode.prevAngle = packet.readFloat()
    }
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
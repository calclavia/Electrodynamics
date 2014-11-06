package resonantinduction.electrical.generator

import java.util.HashSet

import net.minecraft.block.material.Material
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.IRotatable
import resonant.api.grid.{INode, IUpdate}
import resonant.content.prefab.java.TileNode
import resonant.lib.content.prefab.TElectric
import resonantinduction.mechanical.mech.MechanicalNode

/**
 * A kinetic energy to electrical energy converter.
 *
 * @author Calclavia
 */
class TileMotor extends TileNode(Material.iron) with TElectric with IRotatable
{
  var mechNode = new MechanicalNode(this)

  /** Generator turns KE -> EE. Inverted one will turn EE -> KE. */
  var isInversed: Boolean = true
  private var gearRatio = 0

  normalRender = false
  isOpaqueCube = false

  def toggleGearRatio = ((gearRatio + 1) % 3)

  override def start
  {
    super.start
    if (mechNode != null) mechNode.reconstruct
  }

  override def invalidate
  {
    mechNode.deconstruct
    super.invalidate
  }

  override def update
  {
    super.update
    if (mechNode != null && mechNode.isInstanceOf[IUpdate])
    {
      mechNode.asInstanceOf[IUpdate].update(0.05f)
      if (!isInversed)
      {
        receiveMechanical
      }
      else
      {
        produceMechanical
      }
    }
  }

  def receiveMechanical
  {
    val power: Double = mechNode.getForce(ForgeDirection.UNKNOWN) * mechNode.getAngularSpeed(ForgeDirection.UNKNOWN)
    val receive: Double = 0 // dcNode.addEnergy(ForgeDirection.UNKNOWN, power, true)
    if (receive > 0)
    {
      val percentageUsed: Double = receive / power
      mechNode.apply(this, -mechNode.getForce(ForgeDirection.UNKNOWN) * percentageUsed, -mechNode.getAngularSpeed(ForgeDirection.UNKNOWN) * percentageUsed)
    }
  }

  def produceMechanical
  {
    val extract: Double = 0 //dcNode.removeEnergy(ForgeDirection.UNKNOWN, dcNode.getEnergy(ForgeDirection.UNKNOWN), false)
    if (extract > 0)
    {
      val torqueRatio: Long = ((gearRatio + 1) / 2.2d * (extract)).asInstanceOf[Long]
      if (torqueRatio > 0)
      {
        val maxAngularVelocity: Double = extract / torqueRatio.asInstanceOf[Float]
        val maxTorque: Double = (extract) / maxAngularVelocity
        var setAngularVelocity: Double = maxAngularVelocity
        var setTorque: Double = maxTorque
        val currentTorque: Double = Math.abs(mechNode.getForce(ForgeDirection.UNKNOWN))
        if (currentTorque != 0)
        {setTorque = Math.min(setTorque, maxTorque) * (mechNode.getForce(ForgeDirection.UNKNOWN) / currentTorque)}
        val currentVelo: Double = Math.abs(mechNode.getAngularSpeed(ForgeDirection.UNKNOWN))
        if (currentVelo != 0) setAngularVelocity = Math.min(+setAngularVelocity, maxAngularVelocity) * (mechNode.getAngularSpeed(ForgeDirection.UNKNOWN) / currentVelo)
        mechNode.apply(this, setTorque - mechNode.getForce(ForgeDirection.UNKNOWN), setAngularVelocity - mechNode.getAngularSpeed(ForgeDirection.UNKNOWN))
        // dcNode.removeEnergy(ForgeDirection.UNKNOWN, Math.abs(setTorque * setAngularVelocity).asInstanceOf[Long], true)
      }
    }
  }

  override def getInputDirections: HashSet[ForgeDirection] =
  {
    return getOutputDirections
  }

  override def getOutputDirections: HashSet[ForgeDirection] =
  {
    val dirs: HashSet[ForgeDirection] = new HashSet[ForgeDirection]
    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      if (dir != getDirection && dir != getDirection.getOpposite && dir != ForgeDirection.UNKNOWN) dirs.add(dir)
    }
    return dirs
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    isInversed = nbt.getBoolean("isInversed")
    gearRatio = nbt.getByte("gear")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("isInversed", isInversed)
    nbt.setByte("gear", gearRatio.toByte)
  }

  override def getNode[N <: INode](nodeType: Class[_ <: N], from: ForgeDirection): N =
  {
    if (from == getDirection || from == getDirection.getOpposite)
    {
      if (nodeType.isAssignableFrom(mechNode.getClass))
        return mechNode.asInstanceOf[N]
    }

    return null.asInstanceOf[N]
  }

  override def getNodes(nodes: java.util.List[INode])
  {
    nodes.add(this.mechNode)
    nodes.add(this.dcNode)
  }

  override def toString: String =
  {
    return "[TileMotor]" + x + "x " + y + "y " + z + "z "
  }
}
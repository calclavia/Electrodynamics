package resonantinduction.electrical.generator

import java.util.HashSet

import net.minecraft.block.material.Material
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.IRotatable
import resonant.lib.content.prefab.java.TileElectric
import resonantinduction.core.interfaces.IMechanicalNode
import universalelectricity.api.core.grid.{INode, NodeRegistry}

/**
 * A kinetic energy to electrical energy converter.
 *
 * @author Calclavia
 */
class TileMotor extends TileElectric(Material.iron) with IRotatable {

  var mech_node : IMechanicalNode = NodeRegistry.get(this, classOf[IMechanicalNode])
  /** Generator turns KE -> EE. Inverted one will turn EE -> KE. */
  var isInversed: Boolean = true
  private var gearRatio: Byte = 0

  def toggleGearRatio: Byte = {    
    return ((gearRatio + 1) % 3).asInstanceOf[Byte]
  }

  override def initiate {
    super.initiate
    if (mech_node != null) mech_node.reconstruct
  }

  override def invalidate {
    mech_node.deconstruct
    super.invalidate
  }

  override def update {
    super.update
    if (mech_node != null) {
      mech_node.update(0.05f)
      if (!isInversed) {
        receiveMechanical
      }
      else {
        produceMechanical
      }
    }
  }

  def receiveMechanical {
    val power: Double = mech_node.getEnergy
    val receive: Double = electricNode.addEnergy(power, true)
    if (receive > 0) {
      val percentageUsed: Double = receive / power
      mech_node.apply(this, -mech_node.getTorque * percentageUsed, -mech_node.getAngularSpeed * percentageUsed)
    }
  }

  def produceMechanical {
    val extract: Double = electricNode.removeEnergy(electricNode.getEnergy, false)
    if (extract > 0) {
      val torqueRatio: Long = ((gearRatio + 1) / 2.2d * (extract)).asInstanceOf[Long]
      if (torqueRatio > 0) {
        val maxAngularVelocity: Double = extract / torqueRatio.asInstanceOf[Float]
        val maxTorque: Double = (extract) / maxAngularVelocity
        var setAngularVelocity: Double = maxAngularVelocity
        var setTorque: Double = maxTorque
        val currentTorque: Double = Math.abs(mech_node.getTorque)
        if (currentTorque != 0) setTorque = Math.min(setTorque, maxTorque) * (mech_node.getTorque / currentTorque)
        val currentVelo: Double = Math.abs(mech_node.getAngularSpeed)
        if (currentVelo != 0) setAngularVelocity = Math.min(+setAngularVelocity, maxAngularVelocity) * (mech_node.getAngularSpeed / currentVelo)
        mech_node.apply(this, setTorque - mech_node.getTorque, setAngularVelocity - mech_node.getAngularSpeed)
        electricNode.removeEnergy(Math.abs(setTorque * setAngularVelocity).asInstanceOf[Long], true)
      }
    }
  }

  override def getInputDirections: HashSet[ForgeDirection] = {
    return getOutputDirections
  }

  override def getOutputDirections: HashSet[ForgeDirection] = {
    val dirs: HashSet[ForgeDirection] = new HashSet[ForgeDirection]
    for (dir <- ForgeDirection.VALID_DIRECTIONS) {
      if (dir != getDirection && dir != getDirection.getOpposite && dir != ForgeDirection.UNKNOWN) dirs.add(dir)
    }
    return dirs
  }

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    isInversed = nbt.getBoolean("isInversed")
    gearRatio = nbt.getByte("gear")
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    nbt.setBoolean("isInversed", isInversed)
    nbt.setByte("gear", gearRatio)
  }

  override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode = {
    if (from == getDirection || from == getDirection.getOpposite) {
      if (nodeType.isAssignableFrom(mech_node.getClass)) return mech_node
    }
    return super.getNode(nodeType, from)
  }

  override def toString: String = {
    return "[TileMotor]" + x + "x " + y + "y " + z + "z "
  }

}
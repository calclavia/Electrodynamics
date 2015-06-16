package com.calclavia.edx.mechanical.mech

import com.calclavia.edx.mechanical.mech.grid.MechanicalComponent

/**
 * We assume all the force acting on the gear is 90 degrees.
 *
 * @author Calclavia
 */
abstract class PartMechanical extends PartAbstract with JNormalOcclusion with TFacePart with TPartNodeProvider with TCuboidPart {
	/** Side of the block this is placed on */
	var placementSide: ForgeDirection = ForgeDirection.UNKNOWN
	/** The tier of this mechanical part */
	var tier = 0
	/** Node that handles resonantinduction.mechanical action of the machine */
	private var _mechanicalNode: MechanicalComponent = null

	def preparePlacement(side: Int, itemDamage: Int) {
		this.placementSide = ForgeDirection.getOrientation((side).asInstanceOf[Byte])
		this.tier = itemDamage
	}

	override def write(packet: MCDataOutput, id: Int) {
		super.write(packet, id)

		id match {
			case 0 =>
				val tag = new NBTTagCompound
				save(tag)
				packet.writeNBTTagCompound(tag)
			case 1 => packet.writeFloat(mechanicalNode.angularVelocity.toFloat)
			case 2 =>
				if (mechanicalNode.connections.size() > 0) {
					mechanicalNode.resetAngle()
				}
			//        packet.writeFloat(mechanicalNode.angle.toFloat)
		}
	}

	override def save(nbt: NBTTagCompound) {
		nbt.setByte("side", placementSide.ordinal.asInstanceOf[Byte])
		nbt.setByte("tier", tier.asInstanceOf[Byte])
	}

	override def read(packet: MCDataInput, id: Int) {
		super.read(packet, id)

		id match {
			case 0 =>
				load(packet.readNBTTagCompound())
			case 1 => mechanicalNode.angularVelocity = packet.readFloat()
			case 2 =>
			//        mechanicalNode.prevAngle = packet.readFloat()
			//        mechanicalNode.prevTime = System.currentTimeMillis()
		}
	}

	def mechanicalNode = _mechanicalNode

	def mechanicalNode_=(mech: MechanicalComponent) {
		_mechanicalNode = mech
		mechanicalNode.onVelocityChanged = () => if (world != null) sendPacket(1)
		nodes.add(mechanicalNode)
	}

	override def load(nbt: NBTTagCompound) {
		placementSide = ForgeDirection.getOrientation(nbt.getByte("side"))
		tier = nbt.getByte("tier")
	}

	override def redstoneConductionMap: Int = 0

	override def solid(arg0: Int): Boolean = true

	def getPosition: VectorWorld = new VectorWorld(world, x, y, z)

	override def toString: String = "[" + getClass.getSimpleName + "]" + x + "x " + y + "y " + z + "z " + getSlotMask + "s "

}
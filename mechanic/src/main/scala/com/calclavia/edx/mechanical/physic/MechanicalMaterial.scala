package com.calclavia.edx.mechanical.physic

import nova.core.component.Component


case class MechanicalMaterial(name: String, density: Double, friction: Double, breakingForce: Double) extends Component {
	override def toString = name
}

object MechanicalMaterial {
	val wood = MechanicalMaterial("wood", 0.5, 1, 1)
	val stone = MechanicalMaterial("stone", 3, 2, 2)
	val metal = MechanicalMaterial("iron", 2, 0.5, 3)
}
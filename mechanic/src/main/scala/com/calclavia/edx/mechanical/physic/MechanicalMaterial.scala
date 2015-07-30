package com.calclavia.edx.mechanical.physic

import nova.core.component.Component


case class MechanicalMaterial(density: Double, friction: Double, breakingForce: Double) extends Component

object MechanicalMaterial {
	val wood = MechanicalMaterial(0.5, 1, 1)
	val stone = MechanicalMaterial(3, 2, 2)
	val metal = MechanicalMaterial(2, 0.5, 3)
}
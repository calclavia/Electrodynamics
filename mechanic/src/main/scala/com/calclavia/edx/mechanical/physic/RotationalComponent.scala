package com.calclavia.edx.mechanical.physic

import com.calclavia.edx.mechanical.content.axle.BlockAxle
import com.calclavia.edx.mechanical.content.gear.BlockGear
import com.calclavia.edx.mechanical.physic.grid.MechanicalGrid
import nova.core.block.Block
import nova.core.block.component.Connectable
import nova.core.component.{Component, Require}


object Mechanical {

	@Require(classOf[MechanicalMaterial])
	trait Material extends MechanicalConstantFriction {
		val block: Block

		val size: Double
		lazy val material: MechanicalMaterial =  block.get(classOf[MechanicalMaterial])

		lazy val mass = size * material.density
		lazy val constantFriction = mass * material.friction

	}
}

abstract class Mechanical extends Connectable[Mechanical] {
	def mass: Double
	def friction: Double
}

abstract class MechanicalConstantFriction extends Mechanical {
	final def friction: Double = constantFriction

	val constantFriction: Double
}

@Require(classOf[MechanicalMaterial])
class MechanicalGear(val block: BlockGear) extends MechanicalConstantFriction with Mechanical.Material {
	val size = 1D

}
@Require(classOf[MechanicalMaterial])
class MechanicalAxle(val block: BlockAxle) extends MechanicalConstantFriction with Mechanical.Material {
	val size = 0.25D

}

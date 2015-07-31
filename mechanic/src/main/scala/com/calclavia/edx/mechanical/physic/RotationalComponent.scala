package com.calclavia.edx.mechanical.physic

import com.calclavia.edx.mechanical.physic.grid.MechanicalGrid
import nova.core.block.Block
import nova.core.component.{Component, Require}

@Require(classOf[MechanicalMaterial])
class RotationalComponent(val block: Block) extends Component{
	lazy val material = block.get(classOf[MechanicalMaterial])
	var grid: Option[MechanicalGrid] = None
	var rotation = 0D

}

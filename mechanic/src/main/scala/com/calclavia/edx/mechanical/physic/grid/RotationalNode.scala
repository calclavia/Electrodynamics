package com.calclavia.edx.mechanical.physic.grid

import org.jgrapht.graph.DefaultEdge

trait RotationalNode {
	val mass: Double
	val friction: Double
	private[grid] var relativeSpeed: Option[Double] = None

}
case class GearNode(size: Double) extends RotationalNode {
	val mass = 1D
	val friction = 0.1
}
case class AxleNode() extends RotationalNode {
	val mass = 0.25
	val friction = 0.05
}

case class RotationalEdge(src: AnyRef, tar: AnyRef, forward: Boolean) {

}



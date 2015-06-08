package com.calclavia.edx.optics.beam.fx

import com.calclavia.edx.core.prefab.EntityAgeLike
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{Model, Vertex}
import nova.core.util.Direction

org.apache.commons.math3.geometry.euclidean.threed.Rotation
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * An entity scorch effect
 * @author Calclavia
 */
class EntityScorch(side: Int) extends Entity with EntityAgeLike {
	val renderer = add(new DynamicRenderer)

	val particleScale = 0.2f
	var particleAlpha = 0d

	override def maxAge: Double = 1

	override def getID: String = "scortchFx"

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		particleAlpha = 1 - (time / maxAge) * (time / maxAge)
	}

	renderer.setOnRender(
		(model: Model) => {

			//GL_SRC_ALPHA
			model.blendSFactor = 0x302
			//GL_ Minus One
			model.blendDFactor = 0x303

			/**
			 * Rotate the scorch effect
			 */
			model.rotate(new Rotation(Vector3D.PLUS_J, -Math.PI / 2))

			val rot = Direction.fromOrdinal(side).rotation
			/*match
			{
				case Direction.UNKNOWN =>
				case Direction.UP => Quaternion.fromAxis(Vector3d.PLUS_I, Math.PI/2)
				case Direction.DOWN => Quaternion.fromAxis(Vector3d.PLUS_I , -Math.PI/2)
				case Direction.NORTH => Quaternion.identity
				case Direction.SOUTH => Quaternion.fromAxis(180, 0, 1, 0)
				case Direction.WEST => Quaternion.fromAxis(90, 0, 1, 0)
				case Direction.EAST => Quaternion.fromAxis(-90, 0, 1, 0)
			}*/

			/**
			 * Tessellate scorch
			 */
			val scorch = new Model()
			val renderColor = Color.white

			val endFace = scorch.createFace()
			endFace.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0).setColor(renderColor))
			endFace.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1).setColor(renderColor))
			endFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1).setColor(renderColor))
			endFace.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0).setColor(renderColor))

			scorch.drawFace(endFace)
			endFace.brightness = 1
			scorch.bindAll(OpticsTextures.scorchTexture)
			model.children.add(scorch)
		})

}

package com.calclavia.edx.optics.beam.fx

import com.calclavia.edx.core.prefab.EntityAgeLike
import com.calclavia.edx.optics.content.OpticsTextures
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{Face, VertexModel, Model, Vertex}
import nova.core.util.Direction
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

import scala.collection.convert.wrapAll._

/**
 * An entity scorch effect
 * @author Calclavia
 */
class EntityScorch(side: Int) extends Entity with EntityAgeLike {
	val renderer = add(new DynamicRenderer)

	val particleScale = 0.2f
	var particleAlpha = 0d

	val rot = new Rotation(Vector3D.PLUS_K, Direction.fromOrdinal(side).toVector).revert()

	override def maxAge: Double = 1

	override def getID: String = "scorchFx"

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		particleAlpha = 1 - (time / maxAge) * (time / maxAge)
	}

	renderer.onRender(
		(model: Model) => {

			//GL_SRC_ALPHA
			model.blendSFactor = 0x302
			//GL_ Minus One
			model.blendDFactor = 0x303

			/**
			 * Rotate the scorch effect
			 */
			model.matrix.rotate(rot)
			/**
			 * Tessellate scorch
			 */
			val scorch = new VertexModel()
			val renderColor = Color.white

			val face = new Face()
			face.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0))
			face.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1))
			face.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1))
			face.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0))

			face.vertices.foreach(_.color = renderColor)
			scorch.drawFace(face)
			face.brightness = 1
			scorch.bindAll(OpticsTextures.scorchTexture)
			model.children.add(scorch)
		})

}

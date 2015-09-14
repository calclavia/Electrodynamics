package com.calclavia.edx.optics.beam.fx

import com.calclavia.edx.core.prefab.EntityAgeLike
import com.calclavia.edx.optics.content.OpticsTextures
import com.calclavia.edx.optics.grid.OpticGrid
import nova.core.block.Stateful.LoadEvent
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{Face, MeshModel, Model, Vertex}
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}
import org.apache.commons.math3.util.FastMath

import scala.collection.convert.wrapAll._

/**
 * The laser beam effect for electromagnetic waves
 * @author Calclavia
 */
class EntityLaserBeam(start: Vector3D, end: Vector3D, color: Color, power: Double) extends Entity with EntityAgeLike {

	val renderer = components.add(new DynamicRenderer)

	val energyPercentage = Math.min(power / OpticGrid.maxPower, 1).toFloat
	val detail = (6 + 12 * energyPercentage).toInt
	val rotationSpeed = 18

	val particleAlpha = 0.4 * energyPercentage + 0.6
	val particleScale = 0.13f * energyPercentage + 0.03 * FastMath.random()
	val length = start.distance(end)
	val endSize = particleScale
	val renderColor = color.alpha((particleAlpha * 255).toInt)
	val halfColor = renderColor.alpha(127)

	val midPoint = end.midpoint(start)
	val dir = (end - start).normalize
	val rot = new Rotation(Vector3D.PLUS_J, dir)

	/**
	 * Set position
	 */
	events.on(classOf[LoadEvent]).bind((evt: LoadEvent) => setPosition(midPoint))

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		//setRotation(rotation.crossProduct(Quaternion.fromAxis(Vector3d.PLUS_K, time % (Math.PI * 2 / rotationSpeed) * rotationSpeed + rotationSpeed * deltaTime)))
	}

	renderer.onRender(
		(model: Model) => {
			//GL_SRC_ALPHA
			model.blendSFactor = 0x302
			//GL_ONE
			model.blendDFactor = 0x1

			/**
			 * Rotate the beam
			 */
			model.matrix.rotate(rot)

			/**
			 * Tessellate laser
			 */
			for (a <- 0 to detail) {
				val beam = new MeshModel()
				beam.matrix.rotate(Vector3D.PLUS_J, a * Math.PI * 2 / detail)

				/**
				 * Render Cap
				 */
				val cap = new MeshModel()
				cap.matrix.translate(new Vector3D(0, length / 2 - endSize, 0))

				val capFace = new Face()
				capFace.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0))
				capFace.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1))
				capFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1))
				capFace.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0))

				capFace.vertices.foreach(_.color = renderColor)

				capFace.brightness = 1
				cap.drawFace(capFace)
				cap.bindAll(OpticsTextures.laserStartTexture)
				beam.children.add(cap)

				/**
				 * Render Middle
				 */
				val middle = new MeshModel()

				val middleFace = new Face()
				middleFace.drawVertex(new Vertex(-particleScale, -length / 2 + endSize, 0, 0, 0))
				middleFace.drawVertex(new Vertex(-particleScale, length / 2 - endSize, 0, 0, 1))
				middleFace.drawVertex(new Vertex(particleScale, length / 2 - endSize, 0, 1, 1))
				middleFace.drawVertex(new Vertex(particleScale, -length / 2 + endSize, 0, 1, 0))

				middleFace.vertices.foreach(_.color = halfColor)
				middleFace.brightness = 1
				middle.drawFace(middleFace)
				middle.bindAll(OpticsTextures.laserMiddleTexture)
				beam.children.add(middle)

				/**
				 * Render End
				 */
				val end = new MeshModel()
				end.matrix.translate(new Vector3D(0, -length / 2 + endSize, 0))

				val endFace = new Face()
				endFace.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0))
				endFace.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1))
				endFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1))
				endFace.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0))

				endFace.vertices.foreach(_.color = renderColor)
				end.drawFace(endFace)
				endFace.brightness = 1
				end.bindAll(OpticsTextures.laserEndTexture)
				beam.children.add(end)

				//TODO: Not rendering right texture size
				/**
				 * Render Noise
				 */
				val noise = new MeshModel()

				val noiseFace = new Face()
				noiseFace.drawVertex(new Vertex(-particleScale, -length / 2 + endSize, 0, 0, 0))
				noiseFace.drawVertex(new Vertex(-particleScale, length / 2 - endSize, 0, 0, 1))
				noiseFace.drawVertex(new Vertex(particleScale, length / 2 - endSize, 0, 1, 1))
				noiseFace.drawVertex(new Vertex(particleScale, -length / 2 + endSize, 0, 1, 0))

				noiseFace.vertices.foreach(_.color = halfColor)
				noise.drawFace(noiseFace)
				noiseFace.brightness = 1
				noise.bindAll(OpticsTextures.noiseTexture)
				beam.children.add(noise)

				model.children.add(beam)
			}
		}
	)

	override def maxAge: Double = 0.5
}

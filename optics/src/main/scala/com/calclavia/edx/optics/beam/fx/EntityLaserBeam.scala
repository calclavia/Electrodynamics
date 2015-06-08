package com.calclavia.edx.optics.beam.fx

import com.calclavia.edx.core.prefab.EntityAgeLike
import com.calclavia.edx.optics.grid.OpticGrid
import nova.core.block.Stateful.LoadEvent
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{Model, Vertex}
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

import scala.util.Random

/**
 * The laser beam effect for electromagnetic waves
 * @author Calclavia
 */
class EntityLaserBeam(start: Vector3D, end: Vector3D, color: Color, power: Double) extends Entity with EntityAgeLike {

	val renderer = add(new DynamicRenderer)

	val energyPercentage = Math.min(power / OpticGrid.maxPower, 1).toFloat
	val detail = (6 + 14 * energyPercentage).toInt
	val rotationSpeed = 18

	val rand = new Random()
	val particleAlpha = 0.8 * energyPercentage + 0.2
	val particleScale = 0.13f * energyPercentage + 0.03 * rand.nextDouble()
	val length = start.distance(end)
	val endSize = particleScale
	val modifierTranslation = (length / 2) + endSize
	/**
	 * Set position
	 */
	val midPoint = end.midpoint(start)
	val dir = (end - start).normalize

	loadEvent.add((evt: LoadEvent) => {
		setPosition(midPoint)
	})

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		//setRotation(rotation.crossProduct(Quaternion.fromAxis(Vector3d.PLUS_K, time % (Math.PI * 2 / rotationSpeed) * rotationSpeed + rotationSpeed * deltaTime)))
	}

	renderer.setOnRender(
		(model: Model) => {
			//GL_SRC_ALPHA
			model.blendSFactor = 0x302
			//GL_ONE
			model.blendDFactor = 0x1


			/**
			 * Rotate the beam
			 */
			model.rotate(Rotation.fromDirection(dir))
			model.rotate(Vector3D.PLUS_K, Math.PI / 2)

			val renderColor = color.alpha((particleAlpha * 255).toInt)
			val halfColor = renderColor.alpha(127)

			/**
			 * Tessellate laser
			 */
			for (a <- 0 to detail) {
				val beam = new Model()
				beam.rotate(Vector3D.PLUS_J, a * Math.PI * 2 / detail)

				/**
				 * Render Cap
				 */
				val cap = new Model()
				cap.translate(new Vector3D(0, length / 2 - endSize, 0))

				val capFace = cap.createFace()
				capFace.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0).setColor(renderColor))
				capFace.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1).setColor(renderColor))
				capFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1).setColor(renderColor))
				capFace.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0).setColor(renderColor))

				capFace.brightness = 1
				cap.drawFace(capFace)
				cap.bindAll(OpticsTextures.laserStartTexture)
				beam.children.add(cap)

				/**
				 * Render Middle
				 */
				val middle = new Model()

				val middleFace = middle.createFace()
				middleFace.drawVertex(new Vertex(-particleScale, -length / 2 + endSize, 0, 0, 0).setColor(halfColor))
				middleFace.drawVertex(new Vertex(-particleScale, length / 2 - endSize, 0, 0, 1).setColor(halfColor))
				middleFace.drawVertex(new Vertex(particleScale, length / 2 - endSize, 0, 1, 1).setColor(halfColor))
				middleFace.drawVertex(new Vertex(particleScale, -length / 2 + endSize, 0, 1, 0).setColor(halfColor))

				middle.drawFace(middleFace)
				middleFace.brightness = 1
				middle.bindAll(OpticsTextures.laserMiddleTexture)
				beam.children.add(middle)

				/**
				 * Render End
				 */
				val end = new Model()
				end.translate(new Vector3D(0, -length / 2 + endSize, 0))

				val endFace = end.createFace()
				endFace.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0).setColor(renderColor))
				endFace.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1).setColor(renderColor))
				endFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1).setColor(renderColor))
				endFace.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0).setColor(renderColor))

				end.drawFace(endFace)
				endFace.brightness = 1
				end.bindAll(OpticsTextures.laserEndTexture)
				beam.children.add(end)

				//TODO: Not rendering right texture size
				/**
				 * Render Noise
				 */
				val noise = new Model()

				val noiseFace = noise.createFace()
				noiseFace.drawVertex(new Vertex(-particleScale, -length / 2 + endSize, 0, 0, 0).setColor(halfColor))
				noiseFace.drawVertex(new Vertex(-particleScale, length / 2 - endSize, 0, 0, 1).setColor(halfColor))
				noiseFace.drawVertex(new Vertex(particleScale, length / 2 - endSize, 0, 1, 1).setColor(halfColor))
				noiseFace.drawVertex(new Vertex(particleScale, -length / 2 + endSize, 0, 1, 0).setColor(halfColor))

				noise.drawFace(noiseFace)
				noiseFace.brightness = 1
				noise.bindAll(OpticsTextures.noiseTexture)
				beam.children.add(noise)

				model.children.add(beam)
			}
		}
	)

	override def maxAge: Double = 1

	override def getID: String = "laserFx"
}

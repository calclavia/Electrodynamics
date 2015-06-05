package com.calclavia.edx.electric.circuit.component.laser.fx

import com.calclavia.edx.core.prefab.EntityAgeLike
import com.calclavia.edx.electric.circuit.component.laser.WaveGrid
import com.calclavia.edx.optics.content.OpticsTextures
import com.resonant.lib.WrapFunctions._
import nova.core.block.Stateful.LoadEvent
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{Model, Vertex}
import nova.core.util.transform.matrix.Quaternion
import nova.core.util.transform.vector.Vector3d

/**
 * The laser beam effect for electromagnetic waves
 * @author Calclavia
 */
class EntityLaser(start: Vector3d, end: Vector3d, color: Color, power: Double) extends Entity with EntityAgeLike {

	val renderer = add(new DynamicRenderer)

	val energyPercentage = Math.min(power / WaveGrid.maxPower, 1).toFloat
	// + (0.2 - 0.01) * energyPercentage
	val detail = 15
	val rotationSpeed = 18

	val particleAlpha = 1 / (detail.asInstanceOf[Float] / (5f * energyPercentage))
	val particleScale = 0.15f * energyPercentage
	val length = start.distance(end)
	val endSize = particleScale
	val modifierTranslation = (length / 2) + endSize
	/**
	 * Set position
	 */
	val midPoint = (end + start) / 2

	loadEvent.add((evt: LoadEvent) => {
		setPosition(midPoint)
		val difference = end - start
		val rot = Quaternion.fromAxis(Vector3d.xAxis, Vector3d.xAxis.angle(difference))
			.multiply(Quaternion.fromAxis(Vector3d.yAxis, Vector3d.yAxis.angle(difference)))
			.multiply(Quaternion.fromAxis(Vector3d.zAxis, Vector3d.zAxis.angle(difference)))
		setRotation(rot)
	})

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		setRotation(rotation.multiply(Quaternion.fromAxis(Vector3d.xAxis, time % (Math.PI * 2 / rotationSpeed) * rotationSpeed + rotationSpeed * deltaTime)))
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
			model.rotate(Vector3d.xAxis, Math.PI / 2)

			val renderColor = color.alpha((particleAlpha * 255).toInt)

			/**
			 * Tessellate laser
			 */
			for (a <- 0 to detail) {
				val beam = new Model()
				beam.rotate(Vector3d.yAxis, a * Math.PI * 2 / detail)

				/**
				 * Render Cap
				 */
				val cap = new Model()
				cap.translate(new Vector3d(0, length / 2 - endSize, 0))

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
				middleFace.drawVertex(new Vertex(-particleScale, -length / 2 + endSize, 0, 0, 0).setColor(renderColor))
				middleFace.drawVertex(new Vertex(-particleScale, length / 2 - endSize, 0, 0, 1).setColor(renderColor))
				middleFace.drawVertex(new Vertex(particleScale, length / 2 - endSize, 0, 1, 1).setColor(renderColor))
				middleFace.drawVertex(new Vertex(particleScale, -length / 2 + endSize, 0, 1, 0).setColor(renderColor))

				middle.drawFace(middleFace)
				middleFace.brightness = 1
				middle.bindAll(OpticsTextures.laserMiddleTexture)
				beam.children.add(middle)

				/**
				 * Render End
				 */
				val end = new Model()
				end.translate(new Vector3d(0, -length / 2 + endSize, 0))

				val endFace = end.createFace()
				endFace.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0).setColor(renderColor))
				endFace.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1).setColor(renderColor))
				endFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1).setColor(renderColor))
				endFace.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0).setColor(renderColor))

				end.drawFace(endFace)
				endFace.brightness = 1
				end.bindAll(OpticsTextures.laserEndTexture)
				beam.children.add(end)

				//TODO: Not rendering anything
				/**
				 * Render Noise
				 */
				val noise = new Model()
				//noise.translate(new Vector3d(0, -modifierTranslation, 0))
				//noise.rotate(Vector3d.xAxis, Math.PI)

				val noiseFace = noise.createFace()
				noiseFace.drawVertex(new Vertex(-particleScale, -length / 2 + endSize, 0, 0, 0).setColor(renderColor))
				noiseFace.drawVertex(new Vertex(-particleScale, length / 2 - endSize, 0, 0, 1).setColor(renderColor))
				noiseFace.drawVertex(new Vertex(particleScale, length / 2 - endSize, 0, 1, 1).setColor(renderColor))
				noiseFace.drawVertex(new Vertex(particleScale, -length / 2 + endSize, 0, 1, 0).setColor(renderColor))

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

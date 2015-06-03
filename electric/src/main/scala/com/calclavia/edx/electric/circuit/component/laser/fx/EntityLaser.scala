package com.calclavia.edx.electric.circuit.component.laser.fx

import com.calclavia.edx.core.prefab.EntityAgeLike
import com.calclavia.edx.electric.circuit.component.laser.WaveGrid
import com.calclavia.edx.mffs.content.Textures
import com.resonant.lib.WrapFunctions._
import nova.core.block.Stateful.LoadEvent
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{Model, Vertex}
import nova.core.util.transform.vector.Vector3d

/**
 * The laser beam effect for electromagnetic waves
 * @author Calclavia
 */
class EntityLaser(start: Vector3d, end: Vector3d, color: Color, energy: Double) extends Entity with EntityAgeLike {

	val renderer = add(new DynamicRenderer)

	val energyPercentage = Math.min(energy / WaveGrid.maxEnergy, 1).toFloat
	val endSize = 0.01
	// + (0.2 - 0.01) * energyPercentage
	val detail = 20
	val rotationSpeed = 18

	val particleAlpha = 1 / (detail.asInstanceOf[Float] / (5f * energyPercentage))
	val particleScale = 0.4f * energyPercentage
	val length = start.distance(end)
	val difference = end - start
	val angle = end.angle(start)
	val axis = end * start
	val modifierTranslation = (length / 2) + endSize
	/**
	 * Set position
	 */
	val midPoint = (end + start) / 2

	loadEvent.add((evt: LoadEvent) => {
		setPosition(midPoint)
		setScale(Vector3d.one * particleScale)
	})

	renderer.setOnRender(
		(model: Model) => {
			//glBlendFunc(GL_SRC_ALPHA, GL_ONE)
			//glEnable(3042);
			//glColor4f(1, 1, 1, 1)

			/*
			/**
			 * Translation
			 */
			val tX = this.prevPosX + (this.posX - this.prevPosX) * par2 - EntityFX.interpPosX
			val tY = this.prevPosY + (this.posY - this.prevPosY) * par2 - EntityFX.interpPosY
			val tZ = this.prevPosZ + (this.posZ - this.prevPosZ) * par2 - EntityFX.interpPosZ
			model.translate(tX, tY, tZ)
			*/

			/**
			 * Rotate the beam
			 */
			model.rotate(axis, angle)
			//glRotated(90, 1, 0, 0)
			val frameRate = 1
			model.rotate(Vector3d.yAxis, time % (360 / rotationSpeed) * rotationSpeed + rotationSpeed * frameRate)

			val renderColor = color.alpha((particleAlpha * 255).toInt)

			/**
			 * Tessellate laser
			 */
			for (a <- 0 to detail) {
				val beam = new Model()
				beam.rotate(Vector3d.yAxis, a * 360 / detail)

				/**
				 * Render Cap
				 */
				val cap = new Model()
				cap.translate(new Vector3d(0, -modifierTranslation, 0))
				cap.rotate(Vector3d.xAxis, Math.PI)

				val capFace = cap.createFace()
				capFace.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0).setColor(renderColor))
				capFace.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1).setColor(renderColor))
				capFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1).setColor(renderColor))
				capFace.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0).setColor(renderColor))

				capFace.brightness = 1
				cap.drawFace(capFace)
				cap.bindAll(Textures.laserStartTexture)
				beam.children.add(cap)

				/**
				 * Render Middle
				 */
				val middle = new Model()
				middle.translate(new Vector3d(0, -modifierTranslation, 0))
				middle.rotate(Vector3d.xAxis, Math.PI)

				val middleFace = middle.createFace()
				middleFace.drawVertex(new Vertex(-particleScale, -length / 2 + endSize, 0, 0, 0).setColor(renderColor))
				middleFace.drawVertex(new Vertex(-particleScale, length / 2 - endSize, 0, 0, 1).setColor(renderColor))
				middleFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1).setColor(renderColor))
				middleFace.drawVertex(new Vertex(particleScale, -length / 2 + endSize, 0, 1, 0).setColor(renderColor))

				middle.drawFace(middleFace)
				middleFace.brightness = 1
				middle.bindAll(Textures.laserMiddleTexture)
				beam.children.add(middle)

				/**
				 * Render End
				 */
				val end = new Model()
				end.translate(new Vector3d(0, -modifierTranslation, 0))
				end.rotate(Vector3d.xAxis, Math.PI)

				val endFace = end.createFace()
				endFace.drawVertex(new Vertex(-particleScale, -particleScale, 0, 0, 0).setColor(renderColor))
				endFace.drawVertex(new Vertex(-particleScale, particleScale, 0, 0, 1).setColor(renderColor))
				endFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1).setColor(renderColor))
				endFace.drawVertex(new Vertex(particleScale, -particleScale, 0, 1, 0).setColor(renderColor))

				end.drawFace(endFace)
				endFace.brightness = 1
				end.bindAll(Textures.laserEndTexture)
				beam.children.add(end)

				/**
				 * Render Noise
				 */
				val noise = new Model()
				noise.translate(new Vector3d(0, -modifierTranslation, 0))
				noise.rotate(Vector3d.xAxis, Math.PI)

				val noiseFace = noise.createFace()
				noiseFace.drawVertex(new Vertex(-particleScale, -length / 2 + endSize, 0, 0, 0).setColor(renderColor))
				noiseFace.drawVertex(new Vertex(-particleScale, length / 2 - endSize, 0, 0, 1).setColor(renderColor))
				noiseFace.drawVertex(new Vertex(particleScale, particleScale, 0, 1, 1).setColor(renderColor))
				noiseFace.drawVertex(new Vertex(particleScale, -length / 2 + endSize, 0, 1, 0).setColor(renderColor))

				noise.drawFace(noiseFace)
				noiseFace.brightness = 1
				noise.bindAll(Textures.laserNoiseTexture)
				beam.children.add(noise)

				model.children.add(beam)
			}
		}
	)

	override def maxAge: Double = 1

	override def getID: String = "laserFx"
}

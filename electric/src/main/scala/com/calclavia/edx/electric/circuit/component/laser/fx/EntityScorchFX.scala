package com.calclavia.edx.electric.circuit.component.laser.fx

import com.calclavia.edx.core.prefab.EntityAgeLike
import com.calclavia.edx.optics.content.Textures
import com.resonant.lib.WrapFunctions._
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{Model, Vertex}
import nova.core.util.Direction

/**
 * An entity scorch effect
 * @author Calclavia
 */
class EntityScorchFX(side: Int) extends Entity with EntityAgeLike
{
	val renderer = add(new DynamicRenderer)

	val particleScale = 0.2f
	var particleAlpha = 0d

	override def maxAge: Double = 10

	override def getID: String = "scortchFx"

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		particleAlpha = 1 - (time / maxAge) * (time / maxAge)
	}

	renderer.setOnRender(
		(model: Model) => {
			// glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

			/**
			 * Translation
			 See LaserFX
			val f11 = this.prevPosX + (this.posX - this.prevPosX) * par2 - EntityFX.interpPosX
			val f12 = this.prevPosY + (this.posY - this.prevPosY) * par2 - EntityFX.interpPosY
			val f13 = this.prevPosZ + (this.posZ - this.prevPosZ) * par2 - EntityFX.interpPosZ

			glTranslated(f11, f12, f13)
			 */

			/**
			 * Rotate the scorch effect
			 */
			val rot = Direction.fromOrdinal(side).rotation
			/*match
			{
				case Direction.UNKNOWN =>
				case Direction.UP => Quaternion.fromAxis(Vector3d.xAxis, Math.PI/2)
				case Direction.DOWN => Quaternion.fromAxis(Vector3d.xAxis , -Math.PI/2)
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
			scorch.bindAll(Textures.scortchTexture)
			model.children.add(scorch)
		})

}

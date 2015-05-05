package mffs.particle

import com.resonant.core.prefab.block.Updater
import nova.core.block.components.DynamicRenderer
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{Model, Vertex}
import nova.core.render.texture.Texture
import nova.core.util.transform.Vector3d

import scala.beans.BeanProperty
import scala.collection.convert.wrapAll._

/**
 * MFFS Beam Renderer.
 * @author Calclavia, Azanor
 */
abstract class FXBeam(texture: Texture, @BeanProperty var color: Color, maxAge: Double) extends Entity with DynamicRenderer with Updater {

	private val endModifier: Float = 1.0F
	private val reverse: Boolean = false
	private val pulse: Boolean = true
	private val rotationSpeed: Int = 20
	private var target: Vector3d = _
	private var length: Double = _
	/**
	 * Angles are in radians
	 */
	private var rotYaw = 0d
	private var rotPitch = 0d
	private var prevYaw = 0d
	private var prevPitch = 0d
	private var prevSize = 0d
	private var age: Double = 0

	private var prevPos: Vector3d = null

	def setTarget(target: Vector3d): this.type = {
		this.target = target
		length = position.distance(this.target)

		val diff = position - target
		val horizontalDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z)
		this.rotYaw = Math.atan2(diff.x, diff.z)
		this.rotPitch = Math.atan2(diff.y, horizontalDist)
		this.prevYaw = this.rotYaw
		this.prevPitch = this.rotPitch
		return this
	}

	override def getID: String = "fxBeam"

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		prevPos = position

		this.prevYaw = this.rotYaw
		this.prevPitch = this.rotPitch
		val xd: Float = (position.x - target.x).asInstanceOf[Float]
		val yd: Float = (position.y - target.y).asInstanceOf[Float]
		val zd: Float = (position.z - target.z).asInstanceOf[Float]
		this.length = Math.sqrt(xd * xd + yd * yd + zd * zd)
		val var7 = Math.sqrt(xd * xd + zd * zd)
		this.rotYaw = Math.atan2(xd, zd)
		this.rotPitch = Math.atan2(yd, var7)

		age += deltaTime

		if (age >= maxAge) {
			world.destroyEntity(this)
		}
	}

	override def renderDynamic(model: Model) {

		//		GL11.glPushMatrix
		var f = 1
		val var9: Float = 1.0F
		val slide: Float = ticks
		val rot: Float = ticks % (360 / this.rotationSpeed) * this.rotationSpeed + this.rotationSpeed * f
		var size = 1.0
		if (this.pulse) {
			size = Math.min(age, 1.0F)
			size = this.prevSize + (size - this.prevSize) * f
		}

		var op = 0.5
		if (pulse && (this.maxAge - this.age <= 4)) {
			op = 0.5F - (4 - (this.maxAge - this.age)) * 0.1F
		}
		//		GL11.glTexParameterf(3553, 10242, 10497.0F)
		//		GL11.glTexParameterf(3553, 10243, 10497.0F)
		//		GL11.glDisable(2884)
		var var11: Float = slide + f
		if (this.reverse) {
			var11 *= -1.0F
		}
		val var12 = -var11 * 0.2F - Math.floor(-var11 * 0.1F)
		//		GL11.glEnable(3042)
		//		GL11.glBlendFunc(770, 1)
		//		GL11.glDepthMask(false)
		//		GL11.glTranslated(xx, yy, zz)
		val ry = this.prevYaw + (this.rotYaw - this.prevYaw) * f
		val rp = this.prevPitch + (this.rotPitch - this.prevPitch) * f

		model.rotate(Vector3d.xAxis, Math.PI)
		model.rotate(-Vector3d.zAxis, Math.PI * 2 + ry)
		model.rotate(Vector3d.xAxis, rp)

		val var44: Double = -0.15D * size
		val var17: Double = 0.15D * size
		val var44b: Double = -0.15D * size * this.endModifier
		val var17b: Double = 0.15D * size * this.endModifier

		model.rotate(Vector3d.yAxis, rot)

		for (t <- 0 until 3) {

			val var29: Double = this.length * size * var9
			val var31: Double = 0.0D
			val var33: Double = 1.0D
			val var35: Double = -1.0F + var12 + t / 3.0F
			val var37: Double = this.length * size * var9 + var35

			val beamModel = new Model()
			beamModel.rotate(Vector3d.yAxis, Math.PI / 3)
			val face = beamModel.createFace()
			face.drawVertex(new Vertex(var44b, var29, 0.0D, var33, var37))
			face.drawVertex(new Vertex(var44, 0.0D, 0.0D, var33, var35))
			face.drawVertex(new Vertex(var17, 0.0D, 0.0D, var31, var35))
			face.drawVertex(new Vertex(var17b, var29, 0.0D, var31, var37))
			face.vertices.foreach(_.setColor(color.alpha((op * 255).toInt)))
			beamModel.drawFace(face)
			model.children.add(beamModel)
		}

		//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
		//		GL11.glDepthMask(true)
		//		GL11.glDisable(3042)
		//		GL11.glEnable(2884)
		//		GL11.glPopMatrix

		this.prevSize = size
		model.bindAll(texture)
	}
}
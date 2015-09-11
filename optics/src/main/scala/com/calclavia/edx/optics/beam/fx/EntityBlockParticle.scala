package com.calclavia.edx.optics.beam.fx

import nova.core.block.Block
import nova.core.block.Stateful.LoadEvent
import nova.core.component.misc.Collider
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.Entity
import nova.core.entity.component.RigidBody
import nova.core.render.pipeline.BlockRenderStream
import nova.core.util.math.Vector3DUtil
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._

import scala.util.Random

/**
 * A block breaking particle effect
 * @author Calclavia
 */
class EntityBlockParticle(block: Block) extends Entity {

	val renderer = add(new DynamicRenderer)
	val rigidBody = add(classOf[RigidBody])
	val collider = add(new Collider(this))

	val random = new Random()
	val randScale = random.nextDouble() * 0.1 + 0.5

	events.on(classOf[LoadEvent]).bind(
		(evt: LoadEvent) => {
			transform.setScale(Vector3DUtil.ONE * randScale)
			rigidBody.setVelocity(Vector3DUtil.random)
		})

	renderer.onRender(
		//Renders a small version of the block
		//TODO: Does not work yet.
		new BlockRenderStream(block)
			.build()
	)

	/*
	/**
	 * If the block has a colour multiplier, copies it to this particle and returns this particle.
	 */
	def applyColourMultiplier(par1: Int, par2: Int, par3: Int): EntityBlockParticle = {
		if (this.block == Blocks.grass && this.side != 1) {
			return this
		}
		else {
			val l: Int = this.block.colorMultiplier(this.worldObj, par1, par2, par3)
			this.particleRed *= (l >> 16 & 255).asInstanceOf[Float] / 255.0F
			this.particleGreen *= (l >> 8 & 255).asInstanceOf[Float] / 255.0F
			this.particleBlue *= (l & 255).asInstanceOf[Float] / 255.0F
			return this
		}
	}*/

	/*
	override def getFXLayer: Int = {
		return 1
	}*/
	/*
	override def renderParticle(par1Tessellator: Tessellator, par2: Float, par3: Float, par4: Float, par5: Float, par6: Float, par7: Float) {
		var f6: Float = (this.particleTextureIndexX.asInstanceOf[Float] + this.particleTextureJitterX / 4.0F) / 16.0F
		var f7: Float = f6 + 0.015609375F
		var f8: Float = (this.particleTextureIndexY.asInstanceOf[Float] + this.particleTextureJitterY / 4.0F) / 16.0F
		var f9: Float = f8 + 0.015609375F
		val f10: Float = 0.1F * this.particleScale
		if (this.particleIcon != null) {
			f6 = this.particleIcon.getInterpolatedU((this.particleTextureJitterX / 4.0F * 16.0F).asInstanceOf[Double])
			f7 = this.particleIcon.getInterpolatedU(((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F).asInstanceOf[Double])
			f8 = this.particleIcon.getInterpolatedV((this.particleTextureJitterY / 4.0F * 16.0F).asInstanceOf[Double])
			f9 = this.particleIcon.getInterpolatedV(((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F).asInstanceOf[Double])
		}
		val f11: Float = (this.prevPosX + (this.posX - this.prevPosX) * par2.asInstanceOf[Double] - EntityFX.interpPosX).asInstanceOf[Float]
		val f12: Float = (this.prevPosY + (this.posY - this.prevPosY) * par2.asInstanceOf[Double] - EntityFX.interpPosY).asInstanceOf[Float]
		val f13: Float = (this.prevPosZ + (this.posZ - this.prevPosZ) * par2.asInstanceOf[Double] - EntityFX.interpPosZ).asInstanceOf[Float]
		par1Tessellator.setColorOpaque_F(this.particleRed, this.particleGreen, this.particleBlue)
		par1Tessellator.addVertexWithUV((f11 - par3 * f10 - par6 * f10).asInstanceOf[Double], (f12 - par4 * f10).asInstanceOf[Double], (f13 - par5 * f10 - par7 * f10).asInstanceOf[Double], f6.asInstanceOf[Double], f9.asInstanceOf[Double])
		par1Tessellator.addVertexWithUV((f11 - par3 * f10 + par6 * f10).asInstanceOf[Double], (f12 + par4 * f10).asInstanceOf[Double], (f13 - par5 * f10 + par7 * f10).asInstanceOf[Double], f6.asInstanceOf[Double], f8.asInstanceOf[Double])
		par1Tessellator.addVertexWithUV((f11 + par3 * f10 + par6 * f10).asInstanceOf[Double], (f12 + par4 * f10).asInstanceOf[Double], (f13 + par5 * f10 + par7 * f10).asInstanceOf[Double], f7.asInstanceOf[Double], f8.asInstanceOf[Double])
		par1Tessellator.addVertexWithUV((f11 + par3 * f10 - par6 * f10).asInstanceOf[Double], (f12 - par4 * f10).asInstanceOf[Double], (f13 + par5 * f10 - par7 * f10).asInstanceOf[Double], f7.asInstanceOf[Double], f9.asInstanceOf[Double])
	}
	*/
}

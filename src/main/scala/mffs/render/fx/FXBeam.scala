package mffs.render.fx

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.{MathHelper, ResourceLocation}
import net.minecraft.world.World
import nova.core.util.transform.Vector3d
import org.lwjgl.opengl.GL11
import resonantengine.lib.render.RenderUtility

/**
 * Based off Thaumcraft's Beam Renderer.
 *
 * @author Calclavia, Azanor
 */
@SideOnly(Side.CLIENT)
abstract class FXBeam(texture: ResourceLocation, par1World: World, position: Vector3d, target2: Vector3d, red: Float, green: Float, blue: Float, age: Int)
	extends EntityFX(par1World, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D) {
	val xd: Float = (this.posX - this.target.x).toFloat
	val yd: Float = (this.posY - this.target.y).toFloat
	val zd: Float = (this.posZ - this.target.z).toFloat
	val var7: Double = MathHelper.sqrt_double(xd * xd + zd * zd)
	val renderentity: EntityLivingBase = Minecraft.getMinecraft.renderViewEntity
	var visibleDistance: Int = 50
	private[fx] var movX: Double = 0.0D
	private[fx] var movY: Double = 0.0D
	private[fx] var movZ: Double = 0.0D
	private var length: Float = 0.0F
	private var rotYaw: Float = 0.0F
	private var rotPitch: Float = 0.0F
	private var prevYaw: Float = 0.0F
	private var prevPitch: Float = 0.0F

	this.setRGB(red, green, blue)
	this.setSize(0.02F, 0.02F)
	this.noClip = true
	this.motionX = 0.0D
	this.motionY = 0.0D
	this.motionZ = 0.0D
	this.target = target2
	private var target: Vector3d = new Vector3d
	private var endModifier: Float = 1.0F
	private var reverse: Boolean = false
	this.length = new Vector3d(this).distance(this.target).toFloat
	private var pulse: Boolean = true
	this.rotYaw = ((Math.atan2(xd, zd) * 180.0D / 3.141592653589793D).toFloat)
	this.rotPitch = ((Math.atan2(yd, var7) * 180.0D / 3.141592653589793D).toFloat)
	this.prevYaw = this.rotYaw
	this.prevPitch = this.rotPitch
	this.particleMaxAge = age
	private var rotationSpeed: Int = 20
	private var prevSize: Float = 0.0F
	if (!Minecraft.getMinecraft.gameSettings.fancyGraphics) {
		visibleDistance = 25
	}
	if (renderentity.getDistance(this.posX, this.posY, this.posZ) > visibleDistance) {
		this.particleMaxAge = 0
	}

	override def onUpdate {
		this.prevPosX = this.posX
		this.prevPosY = this.posY
		this.prevPosZ = this.posZ
		this.prevYaw = this.rotYaw
		this.prevPitch = this.rotPitch
		val xd: Float = (this.posX - this.target.x).toFloat
		val yd: Float = (this.posY - this.target.y).toFloat
		val zd: Float = (this.posZ - this.target.z).toFloat
		this.length = MathHelper.sqrt_float(xd * xd + yd * yd + zd * zd)
		val var7: Double = MathHelper.sqrt_double(xd * xd + zd * zd)
		this.rotYaw = ((Math.atan2(xd, zd) * 180.0D / 3.141592653589793D).toFloat)
		this.rotPitch = ((Math.atan2(yd, var7) * 180.0D / 3.141592653589793D).toFloat)
		if (({
			this.particleAge += 1;
			this.particleAge - 1
		}) >= this.particleMaxAge) {
			setDead
		}
	}

	def setRGB(r: Float, g: Float, b: Float) {
		this.particleRed = r
		this.particleGreen = g
		this.particleBlue = b
	}

	override def renderParticle(tessellator: Tessellator, f: Float, f1: Float, f2: Float, f3: Float, f4: Float, f5: Float) {
		tessellator.draw
		GL11.glPushMatrix
		val var9: Float = 1.0F
		val slide: Float = this.worldObj.getTotalWorldTime
		val rot: Float = this.worldObj.provider.getWorldTime % (360 / this.rotationSpeed) * this.rotationSpeed + this.rotationSpeed * f
		var size: Float = 1.0F
		if (this.pulse) {
			size = Math.min(this.particleAge / 4.0F, 1.0F)
			size = this.prevSize + (size - this.prevSize) * f
		}
		var op: Float = 0.5F
		if ((this.pulse) && (this.particleMaxAge - this.particleAge <= 4)) {
			op = 0.5F - (4 - (this.particleMaxAge - this.particleAge)) * 0.1F
		}
		FMLClientHandler.instance.getClient.renderEngine.bindTexture(texture)
		GL11.glTexParameterf(3553, 10242, 10497.0F)
		GL11.glTexParameterf(3553, 10243, 10497.0F)
		GL11.glDisable(2884)
		var var11: Float = slide + f
		if (this.reverse) {
			var11 *= -1.0F
		}
		val var12: Float = -var11 * 0.2F - MathHelper.floor_float(-var11 * 0.1F)
		GL11.glEnable(3042)
		GL11.glBlendFunc(770, 1)
		GL11.glDepthMask(false)
		val xx: Float = (this.prevPosX + (this.posX - this.prevPosX) * f - interpPosX).toFloat
		val yy: Float = (this.prevPosY + (this.posY - this.prevPosY) * f - interpPosY).toFloat
		val zz: Float = (this.prevPosZ + (this.posZ - this.prevPosZ) * f - interpPosZ).toFloat
		GL11.glTranslated(xx, yy, zz)
		val ry: Float = this.prevYaw + (this.rotYaw - this.prevYaw) * f
		val rp: Float = this.prevPitch + (this.rotPitch - this.prevPitch) * f
		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F)
		GL11.glRotatef(180.0F + ry, 0.0F, 0.0F, -1.0F)
		GL11.glRotatef(rp, 1.0F, 0.0F, 0.0F)
		val var44: Double = -0.15D * size
		val var17: Double = 0.15D * size
		val var44b: Double = -0.15D * size * this.endModifier
		val var17b: Double = 0.15D * size * this.endModifier
		GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F) {
			var t: Int = 0
			while (t < 3) {
				{
					val var29: Double = this.length * size * var9
					val var31: Double = 0.0D
					val var33: Double = 1.0D
					val var35: Double = -1.0F + var12 + t / 3.0F
					val var37: Double = this.length * size * var9 + var35
					GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F)
					tessellator.startDrawingQuads
					tessellator.setBrightness(200)
					tessellator.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, op)
					tessellator.addVertexWithUV(var44b, var29, 0.0D, var33, var37)
					tessellator.addVertexWithUV(var44, 0.0D, 0.0D, var33, var35)
					tessellator.addVertexWithUV(var17, 0.0D, 0.0D, var31, var35)
					tessellator.addVertexWithUV(var17b, var29, 0.0D, var31, var37)
					tessellator.draw
				}
				({
					t += 1;
					t - 1
				})
			}
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
		GL11.glDepthMask(true)
		GL11.glDisable(3042)
		GL11.glEnable(2884)
		GL11.glPopMatrix
		tessellator.startDrawingQuads
		this.prevSize = size
		FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderUtility.PARTICLE_RESOURCE)
	}
}
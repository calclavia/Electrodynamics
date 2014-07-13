package mffs.render.fx

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.Content
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.{OpenGlHelper, RenderBlocks, Tessellator}
import net.minecraft.world.World
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderUtility
import universalelectricity.core.transform.rotation.Rotation
import universalelectricity.core.transform.vector.Vector3

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
class FXHologramOrbit(par1World: World, orbitPosition: Vector3, position: Vector3, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float) extends FXHologram(par1World, position, red, green, blue, age)
{
  private var rotation: Double = 0

  override def onUpdate
  {
    super.onUpdate
    val xDifference: Double = this.posX - orbitPosition.x
    val yDifference: Double = this.posY - orbitPosition.y
    val zDifference: Double = this.posZ - orbitPosition.z
    val speed: Double = this.maxSpeed * (this.particleAge.asInstanceOf[Float] / this.particleMaxAge.asInstanceOf[Float])
    val originalPosition: Vector3 = new Vector3(this)
    val relativePosition: Vector3 = originalPosition.clone.subtract(this.orbitPosition)
    relativePosition.apply(new Rotation(speed, 0, 0))
    val newPosition: Vector3 = this.orbitPosition.clone.add(relativePosition)
    this.rotation += speed
    this.moveEntity(newPosition.x - originalPosition.x, newPosition.y - originalPosition.y, newPosition.z - originalPosition.z)
  }

  override def renderParticle(tessellator: Tessellator, f: Float, f1: Float, f2: Float, f3: Float, f4: Float, f5: Float)
  {
    tessellator.draw
    GL11.glPushMatrix
    val xx: Float = (this.prevPosX + (this.posX - this.prevPosX) * f - EntityFX.interpPosX).asInstanceOf[Float]
    val yy: Float = (this.prevPosY + (this.posY - this.prevPosY) * f - EntityFX.interpPosY).asInstanceOf[Float]
    val zz: Float = (this.prevPosZ + (this.posZ - this.prevPosZ) * f - EntityFX.interpPosZ).asInstanceOf[Float]
    GL11.glTranslated(xx, yy, zz)
    GL11.glScalef(1.01f, 1.01f, 1.01f)
    GL11.glRotated(-this.rotation, 0, 1, 0)
    var op: Float = 0.5f
    if ((this.particleMaxAge - this.particleAge <= 4))
    {
      op = 0.5f - (5 - (this.particleMaxAge - this.particleAge)) * 0.1F
    }
    GL11.glColor4d(this.particleRed, this.particleGreen, this.particleBlue, op * 2)
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F)
    RenderUtility.enableBlending
    RenderUtility.setTerrainTexture
    RenderUtility.renderNormalBlockAsItem(Content.forceField, 0, new RenderBlocks)
    RenderUtility.disableBlending
    GL11.glPopMatrix
    tessellator.startDrawingQuads
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderUtility.PARTICLE_RESOURCE)
  }
}
package mffs.render.fx

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.ModularForceFieldSystem
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.{OpenGlHelper, RenderBlocks, Tessellator}
import net.minecraft.world.World
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderUtility
import universalelectricity.core.transform.vector.Vector3

@SideOnly(Side.CLIENT)
class FXHologram(par1World: World, position: Vector3, red: Float, green: Float, blue: Float, age: Int) extends FXMFFS(par1World, position.x, position.y, position.z)
{
  private var targetPosition: Vector3 = null

  this.setRBGColorF(red, green, blue)
  this.particleMaxAge = age
  this.noClip = true

  /**
   * The target the hologram is going to translate to.
   *
   * @param targetPosition
   * @return
   */
  def setTarget(targetPosition: Vector3): FXHologram =
  {
    this.targetPosition = targetPosition
    this.motionX = (this.targetPosition.x - this.posX) / this.particleMaxAge
    this.motionY = (this.targetPosition.y - this.posY) / this.particleMaxAge
    this.motionZ = (this.targetPosition.z - this.posZ) / this.particleMaxAge
    return this
  }

  override def onUpdate
  {
    super.onUpdate
    this.prevPosX = this.posX
    this.prevPosY = this.posY
    this.prevPosZ = this.posZ

    particleAge += 1

    if (particleAge - 1 >= this.particleMaxAge)
    {
      this.setDead
      return
    }
    if (this.targetPosition != null)
    {
      this.moveEntity(this.motionX, this.motionY, this.motionZ)
    }
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
    var op: Float = 0.5f
    if ((this.particleMaxAge - this.particleAge <= 4))
    {
      op = 0.5f - (5 - (this.particleMaxAge - this.particleAge)) * 0.1F
    }
    GL11.glColor4d(this.particleRed, this.particleGreen, this.particleBlue, op * 2)
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F)
    RenderUtility.enableBlending
    RenderUtility.setTerrainTexture
    RenderUtility.renderNormalBlockAsItem(ModularForceFieldSystem.Blocks.forceField, 0, new RenderBlocks)
    RenderUtility.disableBlending
    GL11.glPopMatrix
    tessellator.startDrawingQuads
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderUtility.PARTICLE_RESOURCE)
  }
}
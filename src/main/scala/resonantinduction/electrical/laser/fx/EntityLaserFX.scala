package resonantinduction.electrical.laser.fx

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import org.lwjgl.opengl.GL11._
import resonantinduction.core.Reference
import resonantinduction.electrical.ElectricalContent
import resonantinduction.electrical.laser.Laser
import universalelectricity.core.transform.vector.Vector3

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
class EntityLaserFX(par1World: World, start: Vector3, end: Vector3, color: Vector3, energy: Double) extends EntityFX(par1World, start.x, start.y, start.z)
{
  val laserStartTexture = new ResourceLocation(Reference.domain, Reference.FX_DIRECTORY + "laserStart.png")
  val laserMiddleTexture = new ResourceLocation(Reference.domain, Reference.FX_DIRECTORY + "laserMiddle.png")
  val laserEndTexture = new ResourceLocation(Reference.domain, Reference.FX_DIRECTORY + "laserEnd.png")
  val laserNoiseTexture = new ResourceLocation(Reference.domain, Reference.FX_DIRECTORY + "noise.png")

  val energyPercentage = Math.min(energy / Laser.maxEnergy, 1).toFloat

  val endSize = 0.01// + (0.2 - 0.01) * energyPercentage
  val detail = 20
  val rotationSpeed = 18

  /**
   * Set position
   */
  val midPoint = (end + start) / 2
  setPosition(midPoint.x, midPoint.y, midPoint.z)
  lastTickPosX = posX
  lastTickPosY = posY
  lastTickPosZ = posZ

  prevPosX = posX
  prevPosY = posY
  prevPosZ = posZ

  particleScale = 0.4f * energyPercentage
  particleMaxAge = 1
  particleAlpha = 1 / (detail.asInstanceOf[Float] / (5f * energyPercentage))
  particleRed = color.x.toFloat
  particleGreen = color.y.toFloat
  particleBlue = color.z.toFloat

  val length = start.distance(end)

  val difference = end - start
  val angles = difference.toEulerAngle

  val modifierTranslation = (length / 2) + endSize;

  override def onUpdate
  {
    prevPosX = posX
    prevPosY = posY
    prevPosZ = posZ

    if (particleAge >= particleMaxAge)
    {
      setDead
    }

    particleAge += 1
  }

  override def renderParticle(tessellator: Tessellator, par2: Float, par3: Float, par4: Float, par5: Float, par6: Float, par7: Float)
  {
    tessellator.draw()

    glPushMatrix()

    glBlendFunc(GL_SRC_ALPHA, GL_ONE)
    glEnable(3042);
    glColor4f(1, 1, 1, 1)

    /**
     * Translation
     */
    val f11 = this.prevPosX + (this.posX - this.prevPosX) * par2 - EntityFX.interpPosX
    val f12 = this.prevPosY + (this.posY - this.prevPosY) * par2 - EntityFX.interpPosY
    val f13 = this.prevPosZ + (this.posZ - this.prevPosZ) * par2 - EntityFX.interpPosZ

    glTranslated(f11, f12, f13)

    /**
     * Rotate the beam
     */
    glRotated(angles.yaw, 0, 1, 0)
    glRotated(angles.pitch, 1, 0, 0)

    glRotated(90, 1, 0, 0)

    val time = worldObj.getTotalWorldTime()

    /**
     * Tessellate laser
     */
    glPushMatrix()
    glRotatef(time % (360 / rotationSpeed) * rotationSpeed + rotationSpeed * par2, 0, 1, 0)

    for (a <- 0 to detail)
    {
      glRotatef(a * 360 / detail, 0, 1, 0)

      /**
       * Render Cap
       */
      glPushMatrix()
      glTranslated(0, -modifierTranslation, 0)
      glRotatef(180, 1, 0, 0)

      FMLClientHandler.instance.getClient.renderEngine.bindTexture(laserStartTexture)

      tessellator.startDrawingQuads()
      tessellator.setBrightness(200)
      tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha)
      tessellator.addVertexWithUV(-particleScale, -particleScale, 0, 0, 0)
      tessellator.addVertexWithUV(-particleScale, particleScale, 0, 0, 1)
      tessellator.addVertexWithUV(particleScale, particleScale, 0, 1, 1)
      tessellator.addVertexWithUV(particleScale, -particleScale, 0, 1, 0)
      tessellator.draw()

      glPopMatrix()

      /**
       * Render Middle
       */
      glPushMatrix()
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(laserMiddleTexture)

      tessellator.startDrawingQuads()
      tessellator.setBrightness(200)
      tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha)
      tessellator.addVertexWithUV(-particleScale, -length / 2 + endSize, 0, 0, 0)
      tessellator.addVertexWithUV(-particleScale, length / 2 - endSize, 0, 0, 1)
      tessellator.addVertexWithUV(particleScale, length / 2 - endSize, 0, 1, 1)
      tessellator.addVertexWithUV(particleScale, -length / 2 + endSize, 0, 1, 0)
      tessellator.draw()
      glPopMatrix()

      /**
       * Render End
       */
      glPushMatrix()
      glTranslated(0, -modifierTranslation, 0)
      glRotatef(180, 1, 0, 0)

      FMLClientHandler.instance.getClient.renderEngine.bindTexture(laserEndTexture)

      tessellator.startDrawingQuads()
      tessellator.setBrightness(200)
      tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha)
      tessellator.addVertexWithUV(-particleScale, -particleScale , 0, 0, 0)
      tessellator.addVertexWithUV(-particleScale, particleScale, 0, 0, 1)
      tessellator.addVertexWithUV(particleScale, particleScale, 0, 1, 1)
      tessellator.addVertexWithUV(particleScale, -particleScale , 0, 1, 0)
      tessellator.draw()

      glPopMatrix()

      /**
       * Render Noise
       */
      glPushMatrix()
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(laserNoiseTexture)

      tessellator.startDrawingQuads()
      tessellator.setBrightness(200)
      tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha)
      tessellator.addVertexWithUV(-particleScale, -length / 2 + endSize, 0, 0, 0)
      tessellator.addVertexWithUV(-particleScale, length / 2 - endSize, 0, 0, 1)
      tessellator.addVertexWithUV(particleScale, length / 2 - endSize, 0, 1, 1)
      tessellator.addVertexWithUV(particleScale, -length / 2 + endSize, 0, 1, 0)
      tessellator.draw()
      glPopMatrix()
    }

    glPopMatrix()

    glEnable(3042)

    glPopMatrix()

    FMLClientHandler.instance().getClient().renderEngine.bindTexture(ElectricalContent.particleTextures)
    tessellator.startDrawingQuads()
  }

}

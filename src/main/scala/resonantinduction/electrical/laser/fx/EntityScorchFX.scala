package resonantinduction.electrical.laser.fx

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11._
import resonantinduction.electrical.em.ElectromagneticCoherence
import universalelectricity.core.transform.vector.Vector3

/**
 * @author Calclavia
 */

@SideOnly(Side.CLIENT)
class EntityScorchFX(par1World: World, position: Vector3, side: Int) extends EntityFX(par1World, position.x, position.y, position.z)
{
  val texture = new ResourceLocation(ElectromagneticCoherence.DOMAIN, ElectromagneticCoherence.FX_DIRECTORY + "scorch.png")

  lastTickPosX = posX
  lastTickPosY = posY
  lastTickPosZ = posZ

  prevPosX = posX
  prevPosY = posY
  prevPosZ = posZ

  particleScale = 0.2f
  particleMaxAge = 10
  particleAlpha = 1
  particleRed = 1
  particleGreen = 1
  particleBlue = 1

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

    particleAlpha = 1 - (particleAge / particleMaxAge) ^ 2
  }

  override def renderParticle(tessellator: Tessellator, par2: Float, par3: Float, par4: Float, par5: Float, par6: Float, par7: Float)
  {
    tessellator.draw()

    glPushMatrix()
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glColor4f(1, 1, 1, 1)

    /**
     * Translation
     */
    val f11 = this.prevPosX + (this.posX - this.prevPosX) * par2 - EntityFX.interpPosX
    val f12 = this.prevPosY + (this.posY - this.prevPosY) * par2 - EntityFX.interpPosY
    val f13 = this.prevPosZ + (this.posZ - this.prevPosZ) * par2 - EntityFX.interpPosZ

    glTranslated(f11, f12, f13)

    /**
     * Rotate the scorch effect
     */
    ForgeDirection.getOrientation(side) match
    {
      case ForgeDirection.UNKNOWN =>
      case ForgeDirection.UP => glRotatef(90, 1, 0, 0)
      case ForgeDirection.DOWN => glRotatef(-90, 1, 0, 0)
      case ForgeDirection.NORTH => glRotatef(0, 0, 1, 0)
      case ForgeDirection.SOUTH => glRotatef(180, 0, 1, 0)
      case ForgeDirection.WEST => glRotatef(90, 0, 1, 0)
      case ForgeDirection.EAST => glRotatef(-90, 0, 1, 0)
    }

    /**
     * Tessellate scorch
     */
    glPushMatrix()
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(texture)

    tessellator.startDrawingQuads()
    tessellator.setBrightness(200)
    tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha)
    tessellator.addVertexWithUV(-particleScale, -particleScale, 0, 0, 0)
    tessellator.addVertexWithUV(-particleScale, particleScale, 0, 0, 1)
    tessellator.addVertexWithUV(particleScale, particleScale, 0, 1, 1)
    tessellator.addVertexWithUV(particleScale, -particleScale, 0, 1, 0)
    tessellator.draw()

    glPopMatrix()

    glPopMatrix()

    FMLClientHandler.instance().getClient().renderEngine.bindTexture(ElectromagneticCoherence.particleTextures)
    tessellator.startDrawingQuads()
  }

}

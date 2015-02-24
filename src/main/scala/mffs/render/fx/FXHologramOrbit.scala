package mffs.render.fx

import mffs.content.Content

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
class FXHologramOrbit(par1World: World, orbitPosition: Vector3d, position: Vector3d, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
	extends FXHologram(par1World, position, red, green, blue, age)
{
  private var rotation: Double = 0

  override def onUpdate
  {
    super.onUpdate
    val xDifference: Double = this.posX - orbitPosition.x
    val yDifference: Double = this.posY - orbitPosition.y
    val zDifference: Double = this.posZ - orbitPosition.z
    val speed: Double = this.maxSpeed * (this.particleAge.asInstanceOf[Float] / this.particleMaxAge.asInstanceOf[Float])
	  val originalPosition: Vector3d = new Vector3d(this)
	  val relativePosition: Vector3d = originalPosition.clone.subtract(this.orbitPosition)
    relativePosition.transform(new EulerAngle(speed, 0, 0))
	  val newPosition: Vector3d = this.orbitPosition.clone.add(relativePosition)
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
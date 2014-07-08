package mffs.mobilize

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.Reference
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
final object RenderForceMobilizer
{
  val textureOn: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelDirectory + "forceMobilizer_on.png")
  val textureOff: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelDirectory + "forceMobilizer_off.png")
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelDirectory + "forceMobilizer.tcn"))

  def render(tileEntity: TileForceMobilizer, x: Double, y: Double, z: Double, frame: Float)
  {
    if (tileEntity.isActive)
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOn)
    }
    else
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOff)
    }

    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5)
    GL11.glRotatef(180F, 0.0F, 0.0F, 1F)

    tileEntity.getDirection match
    {
      case ForgeDirection.UP =>
        GL11.glRotatef(-90, 1f, 0f, 0)
        GL11.glTranslated(0, -1, 1)
      case ForgeDirection.DOWN =>
        GL11.glRotatef(90, 1f, 0f, 0)
        GL11.glTranslated(0, -1, -1)
      case ForgeDirection.NORTH =>
        GL11.glRotatef(0, 0f, 1f, 0f)
      case ForgeDirection.SOUTH =>
        GL11.glRotatef(180, 0f, 1f, 0f)
      case ForgeDirection.WEST =>
        GL11.glRotatef(-90, 0f, 1f, 0f)
      case ForgeDirection.EAST =>
        GL11.glRotatef(90, 0f, 1f, 0f)
      case _ =>
    }

    model.renderAll
    GL11.glPopMatrix
  }
}
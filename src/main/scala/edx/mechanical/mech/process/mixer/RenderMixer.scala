package edx.mechanical.mech.process.mixer

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.{glPopMatrix, glPushMatrix, glTranslatef}
import resonantengine.api.item.ISimpleItemRenderer
import resonantengine.lib.render.RenderUtility

/**
 * @author Calclavia
 *
 */
@SideOnly(Side.CLIENT)
object RenderMixer extends TileEntitySpecialRenderer with ISimpleItemRenderer
{
  val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "mixer.tcn"))
  var TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "mixer.png")

  def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
  {
    if (t.isInstanceOf[TileMixer])
    {
      val tile: TileMixer = t.asInstanceOf[TileMixer]
      glPushMatrix()
      glTranslatef(x.asInstanceOf[Float] + 0.5F, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5F)

      glPopMatrix()
    }
  }

  def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    glPushMatrix()
    GL11.glScalef(0.25f, 0.25f, 0.25f)
    glTranslatef(0.5F, 0.5f, 0.5f)
    RenderUtility.bind(RenderMixer.TEXTURE)
    RenderMixer.MODEL.renderAll()
    glPopMatrix()
  }
}
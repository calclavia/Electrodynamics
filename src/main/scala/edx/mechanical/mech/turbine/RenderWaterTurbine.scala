package edx.mechanical.mech.turbine

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11
import resonant.api.items.ISimpleItemRenderer
import resonant.lib.render.RenderUtility

@SideOnly(Side.CLIENT) object RenderWaterTurbine
{
  final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "waterTurbines.obj"))
}

@SideOnly(Side.CLIENT) class RenderWaterTurbine extends TileEntitySpecialRenderer with ISimpleItemRenderer
{
  def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
  {
    val tile: TileTurbine = t.asInstanceOf[TileTurbine]
    if (tile.getMultiBlock.isPrimary)
    {
      GL11.glPushMatrix
      GL11.glTranslatef(x.asInstanceOf[Float] + 0.5f, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5f)
      GL11.glPushMatrix
      RenderUtility.rotateBlockBasedOnDirectionUp(tile.getDirection)
      val mechanicalNodeRenderAngle: Double = tile.mechanicalNode.angle
      var renderAngleInDegrees: Float = 0
      if (!java.lang.Double.isNaN(mechanicalNodeRenderAngle))
      {
        renderAngleInDegrees = Math.toDegrees(mechanicalNodeRenderAngle).asInstanceOf[Float]
      }
      GL11.glRotatef(renderAngleInDegrees, 0, 1, 0)
      if (tile.getDirection.offsetY != 0)
      {
        renderWaterTurbine(tile.tier, tile.multiBlockRadius, tile.getMultiBlock.isConstructed)
      }
      else
      {
        renderWaterWheel(tile.tier, tile.multiBlockRadius, tile.getMultiBlock.isConstructed)
      }
      GL11.glPopMatrix
      GL11.glPopMatrix
    }
  }

  def renderWaterWheel(tier: Int, size: Int, isLarge: Boolean)
  {
    if (isLarge)
    {
      GL11.glScalef(0.3f, 1, 0.3f)
      GL11.glScalef(size * 2 + 1, Math.min(size, 2), size * 2 + 1)
      GL11.glPushMatrix
      GL11.glScalef(1, 1.6f, 1)
      RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
      RenderWaterTurbine.MODEL.renderOnly("bigwheel_endknot", "horizontal_centre_shaft")
      GL11.glPopMatrix
      GL11.glPushMatrix
      GL11.glScalef(1, 1.4f, 1)
      RenderUtility.bind(Reference.blockTextureDirectory + "planks_spruce.png")
      RenderWaterTurbine.MODEL.renderOnly("bigwheel_supporters")
      bindTexture(tier)
      RenderWaterTurbine.MODEL.renderOnly("bigwheel_scoops", "bigwheel_supportercircle")
      GL11.glPopMatrix
    }
    else
    {
      GL11.glPushMatrix
      GL11.glScalef(0.7f, 1, 0.7f)
      RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
      RenderWaterTurbine.MODEL.renderOnly("small_waterwheel_endknot")
      bindTexture(tier)
      RenderWaterTurbine.MODEL.renderOnly("small_waterwheel", "small_waterwheel_supporters", "horizontal_centre_shaft")
      GL11.glPopMatrix
    }
  }

  def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    GL11.glPushMatrix
    GL11.glTranslatef(0.5f, 0.5f, 0.5f)
    renderWaterTurbine(itemStack.getItemDamage, 1, false)
    GL11.glPopMatrix
  }

  def renderWaterTurbine(tier: Int, size: Int, isLarge: Boolean)
  {
    if (isLarge)
    {
      GL11.glScalef(0.3f, 1, 0.3f)
      GL11.glScalef(size * 2 + 1, Math.min(size, 2), size * 2 + 1)
      bindTexture(tier)
      RenderWaterTurbine.MODEL.renderOnly("turbine_centre")
      RenderWaterTurbine.MODEL.renderOnly("turbine_blades")
    }
    else
    {
      GL11.glPushMatrix
      GL11.glScalef(0.9f, 1f, 0.9f)
      RenderUtility.bind(Reference.blockTextureDirectory + "log_oak.png")
      RenderWaterTurbine.MODEL.renderOnly("small_waterwheel_endknot")
      bindTexture(tier)
      RenderWaterTurbine.MODEL.renderOnly("small_turbine_blades")
      GL11.glPopMatrix
    }
  }

  def bindTexture(tier: Int)
  {
    if (tier == 0)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
    }
    else if (tier == 1)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
    }
    else if (tier == 2)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "iron_block.png")
    }
  }
}
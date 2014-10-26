package resonantinduction.electrical.transformer

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11
import resonant.content.prefab.scal.render.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT) object RenderTransformer
{
    final val INSTANCE: RenderTransformer = new RenderTransformer
    final val MODEL: WavefrontObject = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "transformer.obj")).asInstanceOf[WavefrontObject]
    final val TEXTURE_COIL: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "transformer_winding.png")
    final val TEXTURE_STONE: ResourceLocation = new ResourceLocation(Reference.blockTextureDirectory + "stone.png")
    final val TEXTURE_IRON: ResourceLocation = new ResourceLocation(Reference.blockTextureDirectory + "iron_block.png")
}

@SideOnly(Side.CLIENT) class RenderTransformer extends ISimpleItemRenderer
{
    def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
    {
        GL11.glTranslated(0, -0.2f, 0)
        doRender
    }

    def doRender
    {
        GL11.glScalef(0.5f, 0.5f, 0.5f)
        FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.TEXTURE_COIL)
        RenderTransformer.MODEL.renderAllExcept("core", "base")
        FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.TEXTURE_IRON)
        RenderTransformer.MODEL.renderOnly("core")
        FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.TEXTURE_STONE)
        RenderTransformer.MODEL.renderOnly("base")
    }

    def render(part: PartElectricTransformer, x: Double, y: Double, z: Double)
    {
        GL11.glPushMatrix
        GL11.glTranslatef(x.asInstanceOf[Float] + 0.5F, y.asInstanceOf[Float] + 0.5F, z.asInstanceOf[Float] + 0.5F)
        RenderUtility.rotateFaceBlockToSide(part.placementSide)
        RenderUtility.rotateBlockBasedOnDirection(part.getFacing)
        GL11.glRotatef(90, 0, 1, 0)
        GL11.glScalef(0.5f, 0.5f, 0.5f)
        FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.TEXTURE_COIL)
        if (part.multiplier == 0)
        {
            RenderTransformer.MODEL.renderOnly("InsulatorLayerLow", "OuterWindingLowBox", "InnerWindingLowBox")
        }
        else if (part.multiplier == 1)
        {
            RenderTransformer.MODEL.renderOnly("InsulatorLayerMed", "OuterWindingMedBox", "InnerWindingMedBox")
        }
        else if (part.multiplier == 2)
        {
            RenderTransformer.MODEL.renderOnly("InnerWindingHighBox", "InsulatorLayerHigh", "OuterWindingHighBox")
        }
        RenderTransformer.MODEL.renderOnly("OuterWindingConnector", "InnerWindingConnector")
        FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.TEXTURE_IRON)
        RenderTransformer.MODEL.renderOnly("core")
        FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.TEXTURE_STONE)
        RenderTransformer.MODEL.renderOnly("base")
        GL11.glPopMatrix
    }
}
package resonantinduction.electrical.generator

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.IModelCustom
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonant.content.prefab.scal.render.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference

/**
 * @author Calclavia
 *
 */
object RenderMotor
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "generator.tcn"))
    final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "generator.png")
}

class RenderMotor extends TileEntitySpecialRenderer with ISimpleItemRenderer
{
    def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
    {
        doRender(t.getBlockMetadata, x, y, z, f)
    }

    private def doRender(facingDirection: Int, x: Double, y: Double, z: Double, f: Float)
    {
        GL11.glPushMatrix
        GL11.glTranslatef(x.asInstanceOf[Float] + 0.5f, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5f)
        GL11.glRotatef(90, 0, 1, 0)
        RenderUtility.rotateBlockBasedOnDirection(ForgeDirection.getOrientation(facingDirection))
        bindTexture(RenderMotor.TEXTURE)
        RenderMotor.MODEL.renderAll
        GL11.glPopMatrix
    }

    def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
    {
        doRender(2, 0, 0, 0, 0)
    }
}
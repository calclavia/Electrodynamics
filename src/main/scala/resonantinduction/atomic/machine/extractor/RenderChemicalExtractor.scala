package resonantinduction.atomic.machine.extractor

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.IModelCustom
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderUtility
import resonant.lib.render.model.FixedTechneModel
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT) object RenderChemicalExtractor
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "chemicalExtractor.tcn"))
    final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "chemicalExtractor.png")
}

@SideOnly(Side.CLIENT) class RenderChemicalExtractor extends TileEntitySpecialRenderer
{
    def render(tileEntity: TileChemicalExtractor, x: Double, y: Double, z: Double, f: Float)
    {
        GL11.glPushMatrix
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
        if (tileEntity.getWorldObj != null)
        {
            RenderUtility.rotateBlockBasedOnDirection(tileEntity.getDirection)
        }
        bindTexture(RenderChemicalExtractor.TEXTURE)
        if (RenderChemicalExtractor.MODEL.isInstanceOf[FixedTechneModel])
        {
            GL11.glPushMatrix
            (RenderChemicalExtractor.MODEL.asInstanceOf[FixedTechneModel]).renderOnlyAroundPivot(Math.toDegrees(tileEntity.rotation), 0, 0, 1, "MAIN CHAMBER-ROTATES", "MAGNET 1-ROTATES", "MAGNET 2-ROTATES")
            GL11.glPopMatrix
            RenderChemicalExtractor.MODEL.renderAllExcept("MAIN CHAMBER-ROTATES", "MAGNET 1-ROTATES", "MAGNET 2-ROTATES")
        }
        else
        {
            RenderChemicalExtractor.MODEL.renderAll
        }
        GL11.glPopMatrix
    }

    def renderTileEntityAt(tileEntity: TileEntity, var2: Double, var4: Double, var6: Double, var8: Float)
    {
        this.render(tileEntity.asInstanceOf[TileChemicalExtractor], var2, var4, var6, var8)
    }
}
package resonantinduction.atomic.machine.centrifuge

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.IModelCustom
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT) object RenderCentrifuge
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "centrifuge.tcn"))
    final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "centrifuge.png")
}

@SideOnly(Side.CLIENT) class RenderCentrifuge extends TileEntitySpecialRenderer
{
    def render(tileEntity: TileCentrifuge, x: Double, y: Double, z: Double, f: Float)
    {
        GL11.glPushMatrix
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
        if (tileEntity.getWorldObj != null)
        {
            RenderUtility.rotateBlockBasedOnDirection(tileEntity.getDirection)
        }
        bindTexture(RenderCentrifuge.TEXTURE)
        GL11.glPushMatrix
        GL11.glRotated(Math.toDegrees(tileEntity.rotation), 0, 1, 0)
        RenderCentrifuge.MODEL.renderOnly("C", "JROT", "KROT", "LROT", "MROT")
        GL11.glPopMatrix
        RenderCentrifuge.MODEL.renderAllExcept("C", "JROT", "KROT", "LROT", "MROT")
        GL11.glPopMatrix
    }

    def renderTileEntityAt(tileEntity: TileEntity, var2: Double, var4: Double, var6: Double, var8: Float)
    {
        this.render(tileEntity.asInstanceOf[TileCentrifuge], var2, var4, var6, var8)
    }
}
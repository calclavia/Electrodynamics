package resonantinduction.atomic.machine.plasma

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.IModelCustom
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderTaggedTile
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT) object RenderPlasmaHeater
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "fusionReactor.tcn"))
    final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "fusionReactor.png")
}

@SideOnly(Side.CLIENT) class RenderPlasmaHeater extends RenderTaggedTile
{
    override def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
    {
        val tileEntity: TilePlasmaHeater = t.asInstanceOf[TilePlasmaHeater]
        if (tileEntity.world != null)
        {
            super.renderTileEntityAt(t, x, y, z, f)
        }
        GL11.glPushMatrix
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
        bindTexture(RenderPlasmaHeater.TEXTURE)
        GL11.glPushMatrix
        GL11.glRotated(Math.toDegrees(tileEntity.rotation), 0, 1, 0)
        RenderPlasmaHeater.MODEL.renderOnly(Array("rrot", "srot") : _*)
        GL11.glPopMatrix
        RenderPlasmaHeater.MODEL.renderAllExcept(Array("rrot", "srot") : _*)
        GL11.glPopMatrix
    }
}
package resonantinduction.mechanical.mech.process.mixer

import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushMatrix
import org.lwjgl.opengl.GL11.glRotatef
import org.lwjgl.opengl.GL11.glTranslatef
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.IModelCustom
import org.lwjgl.opengl.GL11
import resonant.content.prefab.scala.render.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

/**
 * @author Calclavia
 *
 */
@SideOnly(Side.CLIENT) object RenderMixer
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "mixer.tcn"))
    var TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "mixer.png")
}

@SideOnly(Side.CLIENT) class RenderMixer extends TileEntitySpecialRenderer with ISimpleItemRenderer
{
    def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
    {
        if (t.isInstanceOf[TileMixer])
        {
            val tile: TileMixer = t.asInstanceOf[TileMixer]
            glPushMatrix
            glTranslatef(x.asInstanceOf[Float] + 0.5F, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5F)
            RenderUtility.bind(RenderMixer.TEXTURE)
            RenderMixer.MODEL.renderOnly("centerTop", "centerBase")
            glPushMatrix
            glRotatef(Math.toDegrees(tile.mechanicalNode.renderAngle.asInstanceOf[Float]).asInstanceOf[Float], 0, 1, 0)
            RenderMixer. MODEL.renderAllExcept("centerTop", "centerBase")
            glPopMatrix
            glPopMatrix
        }
    }

    def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
    {
        glPushMatrix
        GL11.glScalef(0.5f, 0.5f, 0.5f)
        glTranslatef(0.5F, 0.5f, 0.5f)
        RenderUtility.bind(RenderMixer.TEXTURE)
        RenderMixer.MODEL.renderAll
        glPopMatrix
    }
}
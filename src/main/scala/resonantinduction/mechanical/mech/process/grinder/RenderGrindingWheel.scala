package resonantinduction.mechanical.mech.process.grinder

import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushMatrix
import org.lwjgl.opengl.GL11.glRotatef
import org.lwjgl.opengl.GL11.glScalef
import org.lwjgl.opengl.GL11.glTranslatef
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.IModelCustom
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

/**
 * @author Calclavia
 *
 */
@SideOnly(Side.CLIENT) object RenderGrindingWheel
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "grinder.obj"))
}

@SideOnly(Side.CLIENT) class RenderGrindingWheel extends TileEntitySpecialRenderer
{
    def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
    {
        if (t.isInstanceOf[TileGrindingWheel])
        {
            val tile: TileGrindingWheel = t.asInstanceOf[TileGrindingWheel]
            glPushMatrix
            glTranslatef(x.asInstanceOf[Float] + 0.5F, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5F)
            glScalef(0.51f, 0.5f, 0.5f)
            val dir: ForgeDirection = tile.getDirection
            RenderUtility.rotateBlockBasedOnDirection(dir)
            glRotatef(Math.toDegrees(tile.mechanicalNode.renderAngle).asInstanceOf[Float], 0, 0, 1)
            RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
            RenderGrindingWheel.MODEL.renderAllExcept("teeth")
            RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
            RenderGrindingWheel.MODEL.renderOnly("teeth")
            glPopMatrix
        }
    }
}
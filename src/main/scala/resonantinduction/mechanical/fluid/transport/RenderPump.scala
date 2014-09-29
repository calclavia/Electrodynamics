package resonantinduction.mechanical.fluid.transport

import java.util.{ArrayList, List}

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference

object RenderPump
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "pump.tcn"))
    final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "pump.png")
}

class RenderPump extends TileEntitySpecialRenderer
{
    def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, f: Float)
    {
        val tile: TilePump = tileEntity.asInstanceOf[TilePump]
        GL11.glPushMatrix
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
        GL11.glRotatef(-90, 0, 1, 0)
        if (tile.getWorldObj != null) RenderUtility.rotateBlockBasedOnDirection(tile.getDirection)
        bindTexture(RenderPump.TEXTURE)
        val notRendered: List[String] = new ArrayList[String]
        GL11.glPushMatrix
        GL11.glRotated(Math.toDegrees(tile.mechanicalNode.renderAngle.asInstanceOf[Float]), 0, 0, 1)

        for (i <- 1 to 12)
        {
            val fin: String = "fin" + i
            val innerFin: String = "innerFin" + i
            notRendered.add(fin)
            notRendered.add(innerFin)
            RenderPump.MODEL.renderOnly(fin, innerFin)

        }
        GL11.glPopMatrix
        RenderPump.MODEL.renderAllExcept(notRendered.toArray(new Array[String](0)): _*)
        GL11.glPopMatrix
    }
}
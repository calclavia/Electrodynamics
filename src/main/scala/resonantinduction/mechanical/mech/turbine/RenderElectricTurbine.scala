package resonantinduction.mechanical.mech.turbine

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.apache.commons.lang3.ArrayUtils
import org.lwjgl.opengl.GL11
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT) object RenderElectricTurbine
{
    final val MODEL_SMALL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "turbineSmall.tcn"))
    final val MODEL_LARGE: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "turbineLarge.tcn"))
    final val SMALL_TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "turbineSmall.png")
    final val LARGE_TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "turbineLarge.png")
}

@SideOnly(Side.CLIENT) class RenderElectricTurbine extends TileEntitySpecialRenderer
{
    def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
    {
        val tile: TileTurbine = t.asInstanceOf[TileTurbine]
        if (tile.getMultiBlock.isPrimary)
        {
            GL11.glPushMatrix
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
            if (tile.getMultiBlock.isConstructed)
            {
                bindTexture(RenderElectricTurbine.LARGE_TEXTURE)
                val blades: Array[String] = Array[String]("Blade1", "Blade2", "Blade3", "Blade4", "Blade5", "Blade6")
                val mediumBlades: Array[String] = Array[String]("MediumBlade1", "MediumBlade2", "MediumBlade3", "MediumBlade4", "MediumBlade5", "MediumBlade6")
                val largeBlades: Array[String] = Array[String]("LargeBlade1", "LargeBlade2", "LargeBlade3", "LargeBlade4", "LargeBlade5", "LargeBlade6")
                GL11.glPushMatrix
                GL11.glRotated(Math.toDegrees(tile.mechanicalNode.renderAngle), 0, 1, 0)
                RenderElectricTurbine.MODEL_LARGE.renderOnly(blades: _*)
                RenderElectricTurbine.MODEL_LARGE.renderOnly(largeBlades: _*)
                GL11.glPopMatrix
                GL11.glPushMatrix
                GL11.glRotated(-Math.toDegrees(tile.mechanicalNode.renderAngle), 0, 1, 0)
                RenderElectricTurbine.MODEL_LARGE.renderOnly(mediumBlades: _*)
                GL11.glPopMatrix
                RenderElectricTurbine.MODEL_LARGE.renderAllExcept(ArrayUtils.addAll(ArrayUtils.addAll(blades, mediumBlades: _*), largeBlades: _*): _*)
            }
            else
            {
                GL11.glScalef(1f, 1.1f, 1f)
                bindTexture(RenderElectricTurbine.SMALL_TEXTURE)
                val bladesA: Array[String] = new Array[String](3)

                for (i <- 0 to bladesA.length)
                {
                    bladesA(i) = "BLADE A" + (i + 1) + " SPINS"

                }
                val sheildsA: Array[String] = new Array[String](6)

                for (i <- 0 to sheildsA.length)
                {
                    sheildsA(i) = "SHIELD A" + (i + 1) + " SPINS"

                }
                val bladesB: Array[String] = new Array[String](3)

                for (i <- 0 to bladesB.length)
                {
                    bladesB(i) = "BLADE B" + (i + 1) + " SPINS"

                }
                val sheildsB: Array[String] = new Array[String](6)

                for (i <- 0 to sheildsB.length)
                {
                    sheildsB(i) = "SHIELD B" + (i + 1) + " SPINS"

                }
                val renderA: Array[String] = ArrayUtils.addAll(bladesA, sheildsA: _*)
                val renderB: Array[String] = ArrayUtils.addAll(bladesB, sheildsB: _*)
                GL11.glPushMatrix
                GL11.glRotated(Math.toDegrees(tile.mechanicalNode.renderAngle), 0, 1, 0)
                RenderElectricTurbine.MODEL_SMALL.renderOnly(renderA: _*)
                GL11.glPopMatrix
                GL11.glPushMatrix
                GL11.glRotated(-Math.toDegrees(tile.mechanicalNode.renderAngle), 0, 1, 0)
                RenderElectricTurbine.MODEL_SMALL.renderOnly(renderB: _*)
                GL11.glPopMatrix
                RenderElectricTurbine.MODEL_SMALL.renderAllExcept(ArrayUtils.addAll(renderA, renderB: _*): _*)
            }
            GL11.glPopMatrix
        }
    }
}
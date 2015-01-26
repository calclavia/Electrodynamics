package edx.mechanical.mech.process.crusher

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import net.minecraftforge.common.util.ForgeDirection
import org.apache.commons.lang3.ArrayUtils
import org.lwjgl.opengl.GL11
import resonantengine.lib.render.RenderUtility

@SideOnly(Side.CLIENT) object RenderMechanicalPiston
{
  final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "piston/mechanicalPiston.tcn"))
  var TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "piston/mechanicalPiston_iron.png")
}

@SideOnly(Side.CLIENT) class RenderMechanicalPiston extends TileEntitySpecialRenderer
{
  private[crusher] final val staticParts: Array[String] = Array("baseRing", "leg1", "leg2", "leg3", "leg4", "connector", "basePlate", "basePlateTop", "connectorBar", "centerPiston")
  private[crusher] final val shaftParts: Array[String] = Array("topPlate", "outerPiston")

  def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, f: Float)
  {
    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    val tile: TileMechanicalPiston = tileEntity.asInstanceOf[TileMechanicalPiston]
    GL11.glRotated(-90, 0, 1, 0)
    GL11.glRotated(180, 0, 0, 1)
    if (tile.getWorldObj != null)
    {
      if (tile.getDirection == ForgeDirection.NORTH || tile.getDirection == ForgeDirection.SOUTH) RenderUtility.rotateBlockBasedOnDirection(tile.getDirection.getOpposite)
      else RenderUtility.rotateBlockBasedOnDirection(tile.getDirection)
    }
    RenderUtility.bind(RenderMechanicalPiston.TEXTURE)
    val angle: Double = tile.mechanicalNode.angle
    GL11.glPushMatrix
    GL11.glRotated(-Math.toDegrees(angle), 0, 0, 1)
    RenderMechanicalPiston.MODEL.renderAllExcept(ArrayUtils.addAll(shaftParts, staticParts: _*): _*)
    GL11.glPopMatrix
    GL11.glPushMatrix
    if (tile.getWorldObj != null)
    {
      val dir: ForgeDirection = tile.getDirection
      if (tile.world.isAirBlock(tile.xi + dir.offsetX, tile.yi + dir.offsetY, tile.zi + dir.offsetZ))
      {
        GL11.glTranslated(0, 0, (0.4 * Math.sin(angle)) - 0.5)
      }
      else
      {
        GL11.glTranslated(0, 0, (0.06 * Math.sin(angle)) - 0.03)
      }
    }
    RenderMechanicalPiston.MODEL.renderOnly(shaftParts: _*)
    GL11.glPopMatrix
    RenderMechanicalPiston.MODEL.renderOnly(staticParts: _*)
    GL11.glPopMatrix
  }
}
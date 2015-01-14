package edx.quantum.machine.reactor

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderUtility
import resonant.lib.render.model.ModelCube

@SideOnly(Side.CLIENT) object RenderReactorCell
{
  final val MODEL_TOP: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellTop.tcn"))
  final val MODEL_MIDDLE: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellMiddle.tcn"))
  final val MODEL_BOTTOM: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellBottom.tcn"))
  final val TEXTURE_TOP: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellTop.png")
  final val TEXTURE_MIDDLE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellMiddle.png")
  final val TEXTURE_BOTTOM: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellBottom.png")
  final val TEXTURE_FISSILE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "fissileMaterial.png")
}

@SideOnly(Side.CLIENT) class RenderReactorCell extends TileEntitySpecialRenderer
{
  def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
  {
    val tileEntity: TileReactorCell = t.asInstanceOf[TileReactorCell]
    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    var meta: Int = 2
    if (tileEntity.world != null)
    {
      meta = tileEntity.getBlockMetadata
    }
    val hasBelow: Boolean = tileEntity.world != null && t.getWorldObj.getTileEntity(t.xCoord, t.yCoord - 1, t.zCoord).isInstanceOf[TileReactorCell]

    if (meta == 0)
    {
      bindTexture(RenderReactorCell.TEXTURE_BOTTOM)
      RenderReactorCell.MODEL_BOTTOM.renderAll
    }
    else if (meta == 1)
    {
      bindTexture(RenderReactorCell.TEXTURE_MIDDLE)
      GL11.glTranslatef(0, 0.075f, 0)
      GL11.glScalef(1f, 1.15f, 1f)
      RenderReactorCell.MODEL_MIDDLE.renderAll
    }
    else
    {
      bindTexture(RenderReactorCell.TEXTURE_TOP)
      if (hasBelow)
      {
        GL11.glScalef(1f, 1.32f, 1f)
      }
      else
      {
        GL11.glTranslatef(0, 0.1f, 0)
        GL11.glScalef(1f, 1.2f, 1f)
      }
      if (hasBelow)
      {
        RenderReactorCell.MODEL_TOP.renderAllExcept("BottomPad", "BaseDepth", "BaseWidth", "Base")
      }
      else
      {
        RenderReactorCell.MODEL_TOP.renderAll
      }
    }
    GL11.glPopMatrix
    if (tileEntity.getStackInSlot(0) != null)
    {
      val height: Float = tileEntity.getHeight * ((tileEntity.getStackInSlot(0).getMaxDamage - tileEntity.getStackInSlot(0).getItemDamage).asInstanceOf[Float] / tileEntity.getStackInSlot(0).getMaxDamage.asInstanceOf[Float])
      GL11.glPushMatrix
      GL11.glTranslatef(x.asInstanceOf[Float] + 0.5F, y.asInstanceOf[Float] + 0.5F * height, z.asInstanceOf[Float] + 0.5F)
      GL11.glScalef(0.4f, 0.9f * height, 0.4f)
      bindTexture(RenderReactorCell.TEXTURE_FISSILE)
      RenderUtility.disableLighting
      ModelCube.INSTNACE.render
      RenderUtility.enableLighting
      GL11.glPopMatrix
    }
  }
}
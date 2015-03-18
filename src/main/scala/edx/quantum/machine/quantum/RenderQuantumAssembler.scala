package edx.quantum.machine.quantum

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.entity.{RenderItem, RenderManager}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.entity.item.EntityItem
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT) object RenderQuantumAssembler
{
  final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "quantumAssembler.tcn"))
  final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "quantumAssembler.png")
  final val hands: Array[java.lang.String] = Array[java.lang.String]("Back Arm Upper", "Back Arm Lower", "Right Arm Upper", "Right Arm Lower", "Front Arm Upper", "Front Arm Lower", "Left Arm Upper", "Left Arm Lower")
  final val arms: Array[java.lang.String] = Array[java.lang.String]("Middle Rotor Focus Lazer", "Middle Rotor Uppper Arm", "Middle Rotor Lower Arm", "Middle Rotor Arm Base", "Middle Rotor")
  final val largeArms: Array[java.lang.String] = Array[java.lang.String]("Bottom Rotor Upper Arm", "Bottom Rotor Lower Arm", "Bottom Rotor Arm Base", "Bottom Rotor", "Bottom Rotor Resonator Arm")
  final val all: Array[java.lang.String] = Array[java.lang.String]("Resonance_Crystal", "Back Arm Upper", "Back Arm Lower", "Right Arm Upper", "Right Arm Lower", "Front Arm Upper", "Front Arm Lower", "Left Arm Upper", "Left Arm Lower", "Middle Rotor Focus Lazer", "Middle Rotor Uppper Arm", "Middle Rotor Lower Arm", "Middle Rotor Arm Base", "Middle Rotor", "Bottom Rotor Upper Arm", "Bottom Rotor Lower Arm", "Bottom Rotor Arm Base", "Bottom Rotor", "Bottom Rotor Resonator Arm")

}

@SideOnly(Side.CLIENT) class RenderQuantumAssembler extends TileEntitySpecialRenderer
{
  def renderTileEntityAt(tileEntity: TileEntity, var2: Double, var4: Double, var6: Double, var8: Float)
  {
    this.render(tileEntity.asInstanceOf[TileQuantumAssembler], var2, var4, var6, var8)
  }

  def render(tileEntity: TileQuantumAssembler, x: Double, y: Double, z: Double, f: Float)
  {
    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    bindTexture(RenderQuantumAssembler.TEXTURE)
    GL11.glPushMatrix
    GL11.glRotatef(-tileEntity.rotation.xf, 0, 1f, 0)
    RenderQuantumAssembler.MODEL.renderOnly(RenderQuantumAssembler.hands: _*)
    RenderQuantumAssembler.MODEL.renderOnly("Resonance_Crystal")
    GL11.glPopMatrix
    GL11.glPushMatrix
    GL11.glRotatef(tileEntity.rotation.yf, 0, 1f, 0)
    RenderQuantumAssembler.MODEL.renderOnly(RenderQuantumAssembler.arms: _*)
    GL11.glPopMatrix
    GL11.glPushMatrix
    GL11.glRotatef(-tileEntity.rotation.zf, 0, 1f, 0)
    RenderQuantumAssembler.MODEL.renderOnly(RenderQuantumAssembler.largeArms: _*)
    GL11.glPopMatrix
    RenderQuantumAssembler.MODEL.renderAllExcept(RenderQuantumAssembler.all: _*)
    GL11.glPopMatrix
    val renderItem: RenderItem = (RenderManager.instance.getEntityClassRenderObject(classOf[EntityItem]).asInstanceOf[RenderItem])
    GL11.glPushMatrix
    if (tileEntity.entityItem != null)
    {
      renderItem.doRender(tileEntity.entityItem, x + 0.5, y + 0.4, z + 0.5, 0, 0)
    }
    GL11.glPopMatrix
  }
}
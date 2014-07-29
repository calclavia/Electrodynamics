package resonantinduction.atomic.gate

import codechicken.lib.vec.Cuboid6
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.client.IItemRenderer
import org.lwjgl.opengl.GL11
import resonant.content.prefab.scala.render.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference

object RenderQuantumGlyph {
  final val INSTANCE: RenderQuantumGlyph = new RenderQuantumGlyph
}

class RenderQuantumGlyph extends ISimpleItemRenderer {
  def render(part: PartQuantumGlyph, x: Double, y: Double, z: Double) {
    GL11.glPushMatrix
    GL11.glTranslated(x, y, z)
    val bound: Cuboid6 = part.getBounds
    RenderUtility.bind(TextureMap.locationBlocksTexture)
    RenderUtility.renderCube(bound.min.x, bound.min.y, bound.min.z, bound.max.x, bound.max.y, bound.max.z, Blocks.stone, RenderUtility.getIcon(Reference.prefix + "glyph_" + part.number))
    GL11.glPopMatrix
  }

  def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*) {
    GL11.glPushMatrix
    GL11.glTranslated(-0.25, -0.25, -0.25)
    RenderUtility.bind(TextureMap.locationBlocksTexture)
    RenderUtility.renderCube(0, 0, 0, 0.5, 0.5, 0.5, Blocks.stone, RenderUtility.getIcon(Reference.prefix + "glyph_" + itemStack.getItemDamage))
    GL11.glPopMatrix
  }
}
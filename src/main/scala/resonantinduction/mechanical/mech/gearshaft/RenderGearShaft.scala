package resonantinduction.mechanical.mech.gearshaft

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glRotatef
import resonant.api.items.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference
import resonant.lib.wrapper.ForgeDirectionWrapper._
@SideOnly(Side.CLIENT)
object RenderGearShaft extends ISimpleItemRenderer
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "gears.obj"))

  def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    GL11.glRotatef(90, 1, 0, 0)

    if (itemStack.getItemDamage == 1)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
    }
    else if (itemStack.getItemDamage == 2)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "iron_block.png")
    }
    else
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
    }
    model.renderOnly("Shaft")
  }

  def renderDynamic(part: PartGearShaft, x: Double, y: Double, z: Double, frame: Float)
  {
    GL11.glPushMatrix()
    GL11.glTranslatef(x.asInstanceOf[Float] + 0.5f, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5f)
    GL11.glPushMatrix()
    val dir: ForgeDirection = part.placementSide

    if (dir == ForgeDirection.NORTH)
    {
      glRotatef(90, 1, 0, 0)
    }
    else if (dir == ForgeDirection.WEST)
    {
      glRotatef(-90, 0, 0, 1)
    }

    GL11.glRotated(Math.toDegrees(part.mechanicalNode.angle) * part.placementSide.offset.toArray.sum, 0, 1, 0)

    if (part.tier == 1)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
    }
    else if (part.tier == 2)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "iron_block.png")
    }
    else
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
    }
    model.renderOnly("Shaft")
    GL11.glPopMatrix()
    GL11.glPopMatrix()
  }
}
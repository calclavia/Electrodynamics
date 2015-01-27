package edx.mechanical.fluid.pipe

import java.awt.Color

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.item.{ItemDye, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonantengine.api.item.ISimpleItemRenderer
import resonantengine.lib.render.{FluidRenderUtility, RenderUtility}
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.WorldUtility
import resonantengine.lib.wrapper.BitmaskWrapper._

@SideOnly(Side.CLIENT)
object RenderPipe extends ISimpleItemRenderer
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "pipe.tcn"))
  var texture = new ResourceLocation(Reference.domain, Reference.modelPath + "pipe.png")

  /**
   * Render pipe in world
   */
  def render(part: PartPipe, x: Double, y: Double, z: Double, f: Float)
  {
    GL11.glPushMatrix()
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    render(part.getMaterialID, if (part.getColor > 0) ItemDye.field_150922_c(part.getColor) else -1, part.clientRenderMask)

    val fluid = part.node.getFluid
    val pos = new Vector3(x, y, z)

    if (fluid != null && fluid.amount > 0)
    {
      ForgeDirection.VALID_DIRECTIONS.filter(d => part.clientRenderMask.mask(d.ordinal())).foreach(
        dir =>
        {
          GL11.glPushMatrix()
          GL11.glTranslated(dir.offsetX * 0.33, dir.offsetY * 0.33, dir.offsetZ * 0.33)
          GL11.glScaled(0.33, 0.33, 0.33)
          val tank = part.node
          FluidRenderUtility.renderFluidTesselation(tank, 1, 1, 1, 1)
          GL11.glPopMatrix()
        })
    }

    GL11.glPopMatrix()
  }

  /**
   * Render inventory pipe
   */
  def renderInventoryItem(renderType: ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    GL11.glPushMatrix()

    if (renderType == ItemRenderType.EQUIPPED_FIRST_PERSON || renderType == ItemRenderType.EQUIPPED)
      GL11.glTranslated(0.5, 0.5, 0.5)

    render(itemStack.getItemDamage, -1, 0xC)
    GL11.glPopMatrix()
  }

  /**
   * Render Pipe Model
   */
  def render(meta: Int, colorCode: Int, sides: Int)
  {
    RenderUtility.enableBlending()
    RenderUtility.bind(texture)
    val material = PipeMaterials(meta).asInstanceOf[PipeMaterials.PipeMaterial]
    val matColor = new Color(material.color)

    GL11.glColor4f(matColor.getRed / 255f, matColor.getGreen / 255f, matColor.getBlue / 255f, 1)

    model.renderOnly("Mid")

    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      if (WorldUtility.isEnabledSide(sides, dir))
      {
        GL11.glColor4f(matColor.getRed / 255f, matColor.getGreen / 255f, matColor.getBlue / 255f, 1)
        var prefix: String = null

        dir match
        {
          case ForgeDirection.DOWN =>
            prefix = "Bottom"
          case ForgeDirection.UP =>
            prefix = "Top"
          case ForgeDirection.NORTH =>
            prefix = "Front"
          case ForgeDirection.SOUTH =>
            prefix = "Back"
          case ForgeDirection.WEST =>
            prefix = "Right"
          case ForgeDirection.EAST =>
            prefix = "Left"
          case _ =>
        }

        model.renderOnly(prefix + "Inter", prefix + "Connect")

        if (colorCode > 0)
        {
          val color = new Color(colorCode)
          GL11.glColor4f(color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f, 1)
        }

        model.renderOnly(prefix + "Pipe")
      }
    }

    RenderUtility.disableBlending()
  }
}
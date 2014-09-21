package resonantinduction.mechanical.fluid.pipe

import java.awt.Color

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.{ItemDye, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.opengl.GL11
import resonant.content.prefab.scala.render.ISimpleItemRenderer
import resonant.lib.render.{FluidRenderUtility, RenderUtility}
import resonant.lib.utility.WorldUtility
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT)
object RenderPipe extends ISimpleItemRenderer
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "pipe.tcn"))
  var texture = new ResourceLocation(Reference.domain, Reference.modelPath + "pipe.png")

  /**
   * Render Pipe Model
   */
  def render(meta: Int, colorCode: Int, sides: Int)
  {
    RenderUtility.enableBlending()
    RenderUtility.bind(texture)
    val material: PipeMaterials.PipeMaterial = PipeMaterials.apply(meta).asInstanceOf[PipeMaterials.PipeMaterial]
    val matColor: Color = new Color(material.color)
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

  /**
   * Render pipe in world
   */
  def render(part: PartPipe, x: Double, y: Double, z: Double, f: Float)
  {
    GL11.glPushMatrix()
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    render(part.getMaterialID, if (part.getColor > 0) ItemDye.field_150922_c(part.getColor) else -1, part.connectionMask)
    GL11.glPopMatrix()
    GL11.glPushMatrix()
    val fluid: FluidStack = part.tank.getFluid
    val renderSides: Int = part.connectionMask

    if (fluid != null && fluid.amount > 0)
    {
      GL11.glScaled(0.99, 0.99, 0.99)
      val tank = part.tank
      val percentageFilled: Double = tank.getFluidAmount.asInstanceOf[Double] / tank.getCapacity.asInstanceOf[Double]
      val ySouthEast = percentageFilled
      val yNorthEast = percentageFilled
      val ySouthWest = percentageFilled
      val yNorthWest = percentageFilled
      FluidRenderUtility.renderFluidTesselation(tank, ySouthEast, yNorthEast, ySouthWest, yNorthWest)
    }

    GL11.glPopMatrix()
  }

  /**
   * Render inventory pipe
   */
  def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    GL11.glPushMatrix()
    render(itemStack.getItemDamage, -1, 0xC)
    GL11.glPopMatrix()
  }
}
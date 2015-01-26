package edx.electrical.multimeter

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonantengine.api.items.ISimpleItemRenderer
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.LanguageUtility

/**
 * Class used to render text onto the multimeter block.
 *
 * The more space we have, the more information and detail we render.
 *
 * @author Calclavia
 *
 */
@SideOnly(Side.CLIENT)
object RenderMultimeter extends ISimpleItemRenderer
{
  private final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "multimeter.png")

  def render(part: PartMultimeter, x: Double, y: Double, z: Double)
  {
    val dir: ForgeDirection = part.getDirection
    GL11.glPushMatrix()
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    RenderUtility.rotateFaceBlockToSideOutwards(part.getDirection.getOpposite)
    RenderUtility.bind(TextureMap.locationBlocksTexture)
    RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, Blocks.iron_block, RenderUtility.loadedIconMap.get(Reference.prefix + "multimeter_screen"))
    val metadata: Int = 8
    for (i <- 0 until 6)
    {

      val check: ForgeDirection = ForgeDirection.getOrientation(i)
      if (!part.hasMultimeter(part.x + check.offsetX, part.y + check.offsetY, part.z + check.offsetZ))
      {
        if (dir.offsetX != 0)
        {
          if (check.offsetZ != 0)
          {
            if (dir.offsetX != check.offsetZ) RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, Blocks.iron_block, null, metadata)
            else if (dir.offsetX == check.offsetZ) RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)
          }
          else if (check.offsetY != 0)
          {
            if (check.offsetY > 0) RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, Blocks.iron_block, null, metadata)
            else RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)
          }
        }
        if (dir.offsetZ != 0)
        {
          if (check.offsetX != 0)
          {
            if (dir.offsetZ == check.offsetX) RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, Blocks.iron_block, null, metadata)
            else if (dir.offsetZ != check.offsetX) RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)
          }
          else if (check.offsetY != 0)
          {
            if (check.offsetY > 0) RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, Blocks.iron_block, null, metadata)
            else RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)
          }
        }
        if (dir.offsetY != 0)
        {
          if (check.offsetX != 0)
          {
            if (dir.offsetY != check.offsetX) RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)
            else if (dir.offsetY == check.offsetX) RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, Blocks.iron_block, null, metadata)
          }
          else if (check.offsetZ != 0)
          {
            if (dir.offsetY != check.offsetZ) RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)
            else if (dir.offsetY == check.offsetZ) RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, Blocks.iron_block, null, metadata)
          }
        }
      }
    }

    GL11.glPopMatrix()
    if (part.getGrid.isEnabled && part.isPrimary)
    {
      GL11.glPushMatrix()
      GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
      val centerTranslation: Vector3 = part.getGrid.center - new Vector3(part.x, part.y, part.z) - 0.5
      GL11.glTranslated(centerTranslation.x, centerTranslation.y, centerTranslation.z)
      RenderUtility.rotateFaceBlockToSideOutwards(part.getDirection.getOpposite)
      if (part.getDirection.offsetY != 0)
      {
        GL11.glRotatef(90, 0, 1, 0)
        RenderUtility.rotateBlockBasedOnDirection(part.getFacing)
      }
      GL11.glTranslated(0, 0.05, 0)

      var information = Seq.empty[String]

      for (i <- 0 until part.getGrid.graphs.size)
      {
        if (part.getGrid.graphs(i).head != null /*&& !(part.getGrid.graphs(i).head == part.getGrid.graphs(i))*/ )
        {
          information :+= part.getGrid.getDisplay(i)
        }
      }

      if (information.size <= 0) information :+= LanguageUtility.getLocal("tooltip.noInformation")

      val displacement = 0.72f / information.size
      val maxScale = (part.getGrid.size.x + part.getGrid.size.z).asInstanceOf[Float] * 0.004f

      GL11.glTranslatef(0, 0, -displacement * (information.size / 2f))

      for (i <- 0 until information.size)
      {
        val info = information(i)
        GL11.glPushMatrix()
        GL11.glTranslatef(0, 0, displacement * i)
        if (dir.offsetX != 0) RenderUtility.renderText(info, (part.getGrid.size.z * 0.9f).asInstanceOf[Float], maxScale)
        else if (dir.offsetY != 0) RenderUtility.renderText(info, (Math.min(part.getGrid.size.x, part.getGrid.size.z) * 0.9f).asInstanceOf[Float], maxScale)
        else if (dir.offsetZ != 0) RenderUtility.renderText(info, (part.getGrid.size.x * 0.9f).asInstanceOf[Float], maxScale)
        GL11.glPopMatrix()
      }
      GL11.glPopMatrix()
    }
  }

  def renderInventoryItem(renderType: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    render
  }

  def render()
  {
    GL11.glPushMatrix()
    GL11.glRotatef(90, 1, 0, 0)
    RenderUtility.bind(TextureMap.locationBlocksTexture)
    RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, Blocks.iron_block, RenderUtility.loadedIconMap.get(Reference.prefix + "multimeter_screen"))
    val dir: ForgeDirection = ForgeDirection.NORTH
    val metadata: Int = 8
    RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, Blocks.iron_block, null, metadata)
    RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)

    for (i <- 0 until 6)
    {
      val check: ForgeDirection = ForgeDirection.getOrientation(i)
      if (dir.offsetX != 0 && check.offsetZ != 0)
      {
        if (dir.offsetX != check.offsetZ)
        {
          RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, Blocks.iron_block, null, metadata)
        }
        else if (dir.offsetX == check.offsetZ)
        {
          RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)
        }
      }
      if (dir.offsetZ != 0 && check.offsetX != 0)
      {
        if (dir.offsetZ == check.offsetX)
        {
          RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, Blocks.iron_block, null, metadata)
        }
        else if (dir.offsetZ != check.offsetX)
        {
          RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, Blocks.iron_block, null, metadata)
        }
      }
    }

    GL11.glPopMatrix()
  }
}
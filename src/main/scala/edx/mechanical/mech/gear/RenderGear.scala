package edx.mechanical.mech.gear

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonantengine.api.items.ISimpleItemRenderer
import resonantengine.lib.render.RenderUtility

@SideOnly(Side.CLIENT)
object RenderGear extends ISimpleItemRenderer
{
  final val model: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "gears.obj"))

  def renderDynamic(part: PartGear, x: Double, y: Double, z: Double, tier: Int)
  {
    if (part.getMultiBlock.isPrimary)
    {
      GL11.glPushMatrix()
      GL11.glTranslatef(x.toFloat + 0.5f, y.toFloat + 0.5f, z.toFloat + 0.5f)
      GL11.glPushMatrix()
      renderGear(part.placementSide.ordinal, part.tier, part.getMultiBlock.isConstructed, Math.toDegrees(part.mechanicalNode.angle))
      GL11.glPopMatrix()
      GL11.glPopMatrix()
    }
  }

  def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    GL11.glRotatef(90, 1, 0, 0)
    renderGear(-1, itemStack.getItemDamage, false, 0)
  }

  def renderGear(side: Int, tier: Int, isLarge: Boolean, angle: Double)
  {
    if (tier == 1)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
    }
    else if (tier == 2)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "iron_block.png")
    }
    else if (tier == 10)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "pumpkin_top.png")
    }
    else
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
    }

    RenderUtility.rotateFaceBlockToSide(ForgeDirection.getOrientation(side))
    GL11.glRotated(angle, 0, 1, 0)

    if (isLarge)
      RenderGear.model.renderOnly("LargeGear")
    else
      RenderGear.model.renderOnly("SmallGear")
  }
}
package resonantinduction.mechanical.mech.gear

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.IModelCustom
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonant.content.prefab.scal.render.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT) object RenderGear
{
    final val INSTANCE: RenderGear = new RenderGear
}

@SideOnly(Side.CLIENT)
class RenderGear extends ISimpleItemRenderer
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "gears.obj"))

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
        {
            MODEL.renderOnly("LargeGear")
        }
        else
        {
            MODEL.renderOnly("SmallGear")
        }
    }

    def renderDynamic(part: PartGear, x: Double, y: Double, z: Double, tier: Int)
    {
        if (part.getMultiBlock.isPrimary)
        {
            GL11.glPushMatrix
            GL11.glTranslatef(x.asInstanceOf[Float] + 0.5f, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5f)
            GL11.glPushMatrix
            renderGear(part.placementSide.ordinal, part.tier, part.getMultiBlock.isConstructed, Math.toDegrees(part.node.renderAngle))
            GL11.glPopMatrix
            GL11.glPopMatrix
        }
    }

    def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
    {
        GL11.glRotatef(90, 1, 0, 0)
        renderGear(-1, itemStack.getItemDamage, false, 0)
    }


}
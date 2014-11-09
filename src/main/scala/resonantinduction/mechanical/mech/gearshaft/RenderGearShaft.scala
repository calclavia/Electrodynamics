package resonantinduction.mechanical.mech.gearshaft

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glRotatef
import resonant.content.prefab.scal.render.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT) object RenderGearShaft
{
    final val INSTANCE: RenderGearShaft = new RenderGearShaft
}

@SideOnly(Side.CLIENT) class RenderGearShaft extends ISimpleItemRenderer
{
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
        MODEL.renderOnly("Shaft")
    }

    def renderDynamic(part: PartGearShaft, x: Double, y: Double, z: Double, frame: Float)
    {
        GL11.glPushMatrix
        GL11.glTranslatef(x.asInstanceOf[Float] + 0.5f, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5f)
        GL11.glPushMatrix
        val dir: ForgeDirection = part.placementSide

        if (dir == ForgeDirection.NORTH)
        {
            glRotatef(90, 1, 0, 0)
        }
        else if (dir == ForgeDirection.WEST)
        {
            glRotatef(90, 0, 0, 1)
        }
        GL11.glRotatef(Math.toDegrees(part.mechanicalNode.angle).asInstanceOf[Float], 0, 1, 0)

        if (part.tier == 1)
        {
            RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
        }
        else if (part.tier  == 2)
        {
            RenderUtility.bind(Reference.blockTextureDirectory + "iron_block.png")
        }
        else
        {
            RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
        }
        MODEL.renderOnly("Shaft")
        GL11.glPopMatrix
        GL11.glPopMatrix
    }

    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "gears.obj"))
}
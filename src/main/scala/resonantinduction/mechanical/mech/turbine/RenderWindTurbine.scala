package resonantinduction.mechanical.mech.turbine

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11
import resonant.content.prefab.scala.render.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference

@SideOnly(Side.CLIENT) object RenderWindTurbine
{
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "windTurbines.obj"))
}

@SideOnly(Side.CLIENT) class RenderWindTurbine extends TileEntitySpecialRenderer with ISimpleItemRenderer
{
    def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
    {
        val tile: TileTurbine = t.asInstanceOf[TileTurbine]
        if (tile.getMultiBlock.isPrimary)
        {
            GL11.glPushMatrix
            GL11.glTranslatef(x.asInstanceOf[Float] + 0.5f, y.asInstanceOf[Float] + 0.5f, z.asInstanceOf[Float] + 0.5f)
            GL11.glPushMatrix
            RenderUtility.rotateBlockBasedOnDirectionUp(tile.getDirection)
            GL11.glTranslatef(0, 0.35f, 0)
            GL11.glRotatef(180, 1, 0, 0)
            GL11.glRotatef(Math.toDegrees(tile.mechanicalNode.renderAngle).asInstanceOf[Float], 0, 1, 0)
            render(tile.tier, tile.multiBlockRadius, tile.getMultiBlock.isConstructed)
            GL11.glPopMatrix
            GL11.glPopMatrix
        }
    }

    def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
    {
        GL11.glPushMatrix
        GL11.glTranslatef(0.5f, 0.5f, 0.5f)
        render(itemStack.getItemDamage, 1, false)
        GL11.glPopMatrix
    }

    def render(tier: Int, size: Int, isConstructed: Boolean)
    {
        if (tier == 0)
        {
            RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
        } else if (tier == 1)
        {
            RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
        } else if (tier == 2)
        {
            RenderUtility.bind(Reference.blockTextureDirectory + "iron_block.png")

        }
        if (isConstructed)
        {
            GL11.glScalef(0.3f, 1, 0.3f)
            GL11.glScalef(size * 2 + 1, Math.min(size, 2), size * 2 + 1)
            if (tier == 2)
            {
                GL11.glTranslatef(0, -0.11f, 0)
                RenderWindTurbine.MODEL.renderOnly("LargeMetalBlade")
                RenderWindTurbine.MODEL.renderOnly("LargeMetalHub")
            }
            else
            {
                RenderWindTurbine.MODEL.renderOnly("LargeBladeArm")
                GL11.glScalef(1f, 2f, 1f)
                GL11.glTranslatef(0, -0.05f, 0)
                RenderWindTurbine.MODEL.renderOnly("LargeHub")
                RenderUtility.bind(Reference.blockTextureDirectory + "wool_colored_white.png")
                RenderWindTurbine.MODEL.renderOnly("LargeBlade")
            }
        }
        else
        {
            RenderWindTurbine.MODEL.renderOnly("SmallBlade")
            RenderUtility.bind(Reference.blockTextureDirectory + "log_oak.png")
            RenderWindTurbine.MODEL.renderOnly("SmallHub")
        }
    }
}
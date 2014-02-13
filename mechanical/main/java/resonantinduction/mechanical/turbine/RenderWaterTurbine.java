package resonantinduction.mechanical.turbine;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.prefab.turbine.TileTurbine;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWaterTurbine extends TileEntitySpecialRenderer
{
	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileTurbine tile = (TileTurbine) t;

		if (tile.getMultiBlock().isPrimary())
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
			GL11.glPushMatrix();

			if (tile.worldObj != null)
			{
				RenderUtility.rotateBlockBasedOnDirectionUp(tile.getDirection());
			}

			GL11.glTranslatef(0, -0.35f, 0);
			GL11.glRotatef((float) Math.toDegrees(tile.rotation), 0, 1, 0);

			/**
			 * TODO: Bind based on tier.
			 * cobblestone, iron_block
			 */
			if (tile.getMultiBlock().isConstructed())
			{
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "iron_block.png");
				RenderWindTurbine.MODEL.renderOnly("LargeMetalBlade");
				GL11.glScalef(1f, 2f, 1f);
				GL11.glTranslatef(0, -0.08f, 0);
				RenderWindTurbine.MODEL.renderOnly("LargeMetalHub");
			}
			else
			{
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
				RenderWindTurbine.MODEL.renderOnly("SmallHub");
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
				RenderWindTurbine.MODEL.renderOnly("SmallBlade");
			}

			GL11.glPopMatrix();
			GL11.glPopMatrix();
		}
	}

}
package resonantinduction.mechanical.turbine;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.prefab.turbine.TileTurbine;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWindTurbine extends TileEntitySpecialRenderer
{
	// TODO: Fix model.
	public final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "gears.obj");

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

			GL11.glRotatef((float) Math.toDegrees(tile.rotation), 0, 1, 0);

			/**
			 * TODO: Bind based on tier.
			 */
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");

			if (tile.getMultiBlock().isConstructed())
			{
				MODEL.renderOnly("LargeGear");
			}
			else
			{
				MODEL.renderOnly("SmallGear");
			}

			GL11.glPopMatrix();
			GL11.glPopMatrix();
		}
	}

}
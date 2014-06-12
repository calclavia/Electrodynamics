package resonantinduction.mechanical.energy.turbine;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWaterTurbine extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "waterTurbines.obj");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileTurbine tile = (TileTurbine) t;

		if (tile.getMultiBlock().isPrimary())
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
			GL11.glPushMatrix();

			// Determine if the turbine is facing horizontally or vertical.
			RenderUtility.rotateBlockBasedOnDirectionUp(tile.getDirection());
			
			// Get the rotation directly from the mechanical node running client side.
			double mechanicalNodeRenderAngle = tile.mechanicalNode.renderAngle;
			
			// We need to convert this value into something the model renderer can understand.
			// Note: Check for NaN and if so then just defaults to zero.
			float renderAngleInDegrees = 0;
			if (!Double.isNaN(mechanicalNodeRenderAngle))
			{
			    renderAngleInDegrees = (float) Math.toDegrees(mechanicalNodeRenderAngle);
			}
			
			// Call to actually rotate the gear model to the specified degree.
			GL11.glRotatef(renderAngleInDegrees, 0, 1, 0);
			//ResonantInduction.LOGGER.info("[RenderWaterTurbine] Render Angle: " + renderAngleInDegrees);

			// Determine what type of water turbine model we need to use based on orientation.
			if (tile.getDirection().offsetY != 0)
			{
				renderWaterTurbine(tile.tier, tile.multiBlockRadius, tile.getMultiBlock().isConstructed());
			}
			else
			{
				renderWaterWheel(tile.tier, tile.multiBlockRadius, tile.getMultiBlock().isConstructed());
			}

			GL11.glPopMatrix();
			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5f, 0.5f, 0.5f);
		renderWaterTurbine(itemStack.getItemDamage(), 1, false);
		GL11.glPopMatrix();
	}

	public void renderWaterWheel(int tier, int size, boolean isLarge)
	{
		if (isLarge)
		{
			GL11.glScalef(0.3f, 1, 0.3f);
			GL11.glScalef(size * 2 + 1, Math.min(size, 2), size * 2 + 1);

			GL11.glPushMatrix();
			GL11.glScalef(1, 1.6f, 1);
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
			MODEL.renderOnly("bigwheel_endknot", "horizontal_centre_shaft");
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glScalef(1, 1.4f, 1);
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_spruce.png");
			MODEL.renderOnly("bigwheel_supporters");
			bindTexture(tier);
			MODEL.renderOnly("bigwheel_scoops", "bigwheel_supportercircle");
			GL11.glPopMatrix();

		}
		else
		{
			GL11.glPushMatrix();
			GL11.glScalef(0.7f, 1, 0.7f);
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
			MODEL.renderOnly("small_waterwheel_endknot");
			bindTexture(tier);
			MODEL.renderOnly("small_waterwheel", "small_waterwheel_supporters", "horizontal_centre_shaft");
			GL11.glPopMatrix();
		}
	}

	public void renderWaterTurbine(int tier, int size, boolean isLarge)
	{
		if (isLarge)
		{
			GL11.glScalef(0.3f, 1, 0.3f);
			GL11.glScalef(size * 2 + 1, Math.min(size, 2), size * 2 + 1);
			bindTexture(tier);
			MODEL.renderOnly("turbine_centre");
			MODEL.renderOnly("turbine_blades");
		}
		else
		{
			GL11.glPushMatrix();
			GL11.glScalef(0.9f, 1f, 0.9f);
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "log_oak.png");
			MODEL.renderOnly("small_waterwheel_endknot");
			bindTexture(tier);
			MODEL.renderOnly("small_turbine_blades");
			GL11.glPopMatrix();
		}
	}

	public void bindTexture(int tier)
	{
		switch (tier)
		{
			case 0:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
				break;
			case 1:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
				break;
			case 2:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "iron_block.png");
				break;
		}
	}
}
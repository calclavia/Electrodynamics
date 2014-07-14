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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWindTurbine extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "windTurbines.obj");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileTurbine tile = (TileTurbine) t;

		if (tile.getMultiBlock().isPrimary())
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
			GL11.glPushMatrix();

			RenderUtility.rotateBlockBasedOnDirectionUp(tile.getDirection());

			GL11.glTranslatef(0, 0.35f, 0);
			GL11.glRotatef(180, 1, 0, 0);
			GL11.glRotatef((float) Math.toDegrees(tile.mechanicalNode.renderAngle), 0, 1, 0);

			render(tile.tier, tile.multiBlockRadius, tile.getMultiBlock().isConstructed());

			GL11.glPopMatrix();
			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5f, 0.5f, 0.5f);
		render(itemStack.getItemDamage(), 1, false);
		GL11.glPopMatrix();
	}

	public void render(int tier, int size, boolean isConstructed)
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

		if (isConstructed)
		{
			GL11.glScalef(0.3f, 1, 0.3f);
			GL11.glScalef(size * 2 + 1, Math.min(size, 2), size * 2 + 1);

			if (tier == 2)
			{
				GL11.glTranslatef(0, -0.11f, 0);
				MODEL.renderOnly("LargeMetalBlade");
				MODEL.renderOnly("LargeMetalHub");
			}
			else
			{
				MODEL.renderOnly("LargeBladeArm");
				GL11.glScalef(1f, 2f, 1f);
				GL11.glTranslatef(0, -0.05f, 0);
				MODEL.renderOnly("LargeHub");
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "wool_colored_white.png");
				MODEL.renderOnly("LargeBlade");
			}
		}
		else
		{
			MODEL.renderOnly("SmallBlade");
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "log_oak.png");
			MODEL.renderOnly("SmallHub");
		}

	}
}
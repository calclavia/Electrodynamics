package resonantinduction.electrical.levitator;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.block.ICustomBlockRenderer;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderLevitator extends TileEntitySpecialRenderer implements ICustomBlockRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "levitator.tcn");
	public static final ResourceLocation TEXTURE_ON = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "levitator_on.png");
	public static final ResourceLocation TEXTURE_OFF = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "levitator_off.png");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		RenderUtility.rotateFaceBlockToSide(((TileLevitator) t).getDirection());

		/**
		 * if (((TileLevitator) t).suck)
		 * this.bindTexture(TEXTURE_ON);
		 * else
		 * this.bindTexture(TEXTURE_PUSH);
		 */

		if (((TileLevitator) t).canFunction())
		{
			bindTexture(TEXTURE_ON);
		}
		else
		{
			bindTexture(TEXTURE_OFF);
		}

		MODEL.renderAll();
		GL11.glPopMatrix();
	}

	@Override
	public void renderInventory(Block block, int metadata, int modelID, RenderBlocks renderer)
	{

	}

	@Override
	public boolean renderStatic(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		return false;
	}

	@Override
	public void renderDynamic(TileEntity tile, Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5f, 1.7f, 0.5f);
		GL11.glRotatef(180f, 0f, 0f, 1f);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_OFF);
		MODEL.renderAll();
		GL11.glPopMatrix();
	}
}

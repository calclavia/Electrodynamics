package resonantinduction.electrical.generator;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.block.ICustomBlockRenderer;

/**
 * @author Calclavia
 * 
 */
public class RenderGenerator extends TileEntitySpecialRenderer implements ICustomBlockRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "generator.tcn");
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "generator.png");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		doRender(t, t.getBlockMetadata(), x, y, z, f);
	}

	private void doRender(TileEntity t, int facingDirection, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
		GL11.glRotatef(90, 0, 1, 0);
		RenderUtility.rotateBlockBasedOnDirection(ForgeDirection.getOrientation(facingDirection));
		bindTexture(TEXTURE);
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
		doRender(tile, 2, 0, 0, 0, 0);
	}
}

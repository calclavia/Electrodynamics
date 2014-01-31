package resonantinduction.electrical.generator;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.block.ICustomBlockRenderer;

/**
 * @author Calclavia
 * 
 */
public class RenderGenerator extends TileEntitySpecialRenderer implements ICustomBlockRenderer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "generator.png");
	public static final ModelGenerator MODEL = new ModelGenerator();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		doRender(t, t.getBlockMetadata(), x, y, z, f);
	}

	private void doRender(TileEntity t, int facingDirection, double x, double y, double z, float f)
	{
		glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		bindTexture(TEXTURE);
		GL11.glRotatef(180, 0f, 0f, 1f);

		// Rotate the model
		switch (facingDirection)
		{
			case 0:
				GL11.glRotatef(90, 1, 0, 0);
				GL11.glTranslatef(0, -1, -1);
				break;
			case 1:
				GL11.glRotatef(-90, 1, 0, 0);
				GL11.glTranslatef(0, -1, 1);
				break;
			case 2:
				GL11.glRotatef(180, 0f, 1f, 0f);
				break;
			case 3:
				GL11.glRotatef(0, 0f, 1f, 0f);
				break;
			case 4:
				GL11.glRotatef(90, 0f, 1f, 0f);
				break;
			case 5:
				GL11.glRotatef(-90, 0f, 1f, 0f);
				break;
		}

		MODEL.render(0.0625F);
		glPopMatrix();
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

/**
 * 
 */
package resonantinduction.old.core.render;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import resonantinduction.electrical.battery.BlockBattery;
import resonantinduction.electrical.battery.RenderBattery;
import resonantinduction.electrical.tesla.BlockTesla;
import resonantinduction.electrical.tesla.RenderTesla;
import resonantinduction.old.transport.levitator.BlockLevitator;
import resonantinduction.old.transport.levitator.RenderLevitator;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class BlockRenderingHandler implements ISimpleBlockRenderingHandler
{
	public static final BlockRenderingHandler INSTANCE = new BlockRenderingHandler();
	private static final int ID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		if (block instanceof BlockTesla)
		{
			glPushMatrix();
			glTranslated(0.5, 1.5, 0.5);
			glRotatef(180F, 0.0F, 0.0F, 1.0F);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderTesla.TEXTURE_BOTTOM);
			RenderTesla.MODEL_BOTTOM.render(0.0625f);
			glPopMatrix();
		}
		else if (block instanceof BlockLevitator)
		{
			glPushMatrix();
			glTranslated(0.5, 1.5, 0.5);
			glRotatef(180F, 0.0F, 0.0F, 1.0F);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderLevitator.TEXTURE);
			RenderLevitator.MODEL.render(0.0625f);
			glPopMatrix();
		}
		else if (block instanceof BlockBattery)
		{
			glPushMatrix();
			glTranslatef(0.5f, 0.9f, 0.5f);
			glScalef(0.5f, 0.5f, 0.5f);
			glRotatef(180F, 0.0F, 0.0F, 1.0F);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderBattery.TEXTURE);
			RenderBattery.MODEL.renderAll();
			glPopMatrix();
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return ID;
	}
}

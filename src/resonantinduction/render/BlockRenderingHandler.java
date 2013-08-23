/**
 * 
 */
package resonantinduction.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import resonantinduction.battery.BlockBattery;
import resonantinduction.contractor.BlockEMContractor;
import resonantinduction.multimeter.BlockMultimeter;
import resonantinduction.tesla.BlockTesla;
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
			GL11.glPushMatrix();
			GL11.glTranslated(0.5, 1.5, 0.5);
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			FMLClientHandler.instance().getClient().renderEngine.func_110577_a(RenderTesla.TEXTURE_BOTTOM);
			RenderTesla.MODEL_BOTTOM.render(0.0625f);
			GL11.glPopMatrix();
		}
		else if (block instanceof BlockEMContractor)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(0.5, 1.5, 0.5);
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			FMLClientHandler.instance().getClient().renderEngine.func_110577_a(RenderEMContractor.TEXTURE);
			RenderEMContractor.MODEL.render(0.0625f);
			GL11.glPopMatrix();
		}
		else if (block instanceof BlockBattery)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(0.5, 1.42, 0.5);
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			FMLClientHandler.instance().getClient().renderEngine.func_110577_a(RenderBattery.TEXTURE);
			RenderBattery.MODEL.render(0.0625f);
			GL11.glPopMatrix();
		}
		else if (block instanceof BlockMultimeter)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glTranslated(0, -1, -0.7);
			FMLClientHandler.instance().getClient().renderEngine.func_110577_a(RenderMultimeter.TEXTURE);
			RenderMultimeter.MODEL.render(0.0625f);
			GL11.glPopMatrix();
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

package mffs.render;

import mffs.block.BlockCoercionDeriver;
import mffs.block.BlockForceFieldProjector;
import mffs.block.BlockForceManipulator;
import mffs.block.BlockFortronCapacitor;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import calclavia.lib.render.CalclaviaRenderHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockHandler implements ISimpleBlockRenderingHandler
{
	public static final int ID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		if (modelID == ID)
		{
			GL11.glPushMatrix();

			if (block instanceof BlockFortronCapacitor)
			{
				FMLClientHandler.instance().getClient().renderEngine.func_110577_a(RenderFortronCapacitor.TEXTURE_ON);
				GL11.glTranslated(0.5, 1.9, 0.5);
				GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
				GL11.glScalef(1.3f, 1.3f, 1.3f);
				RenderFortronCapacitor.MODEL.render(0.0625F);
			}
			else if (block instanceof BlockForceFieldProjector)
			{
				FMLClientHandler.instance().getClient().renderEngine.func_110577_a(RenderForceFieldProjector.TEXTURE_ON);
				GL11.glTranslated(0.5, 1.5, 0.5);
				GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
				RenderForceFieldProjector.MODEL.render(0, 0.0625F);
			}
			else if (block instanceof BlockCoercionDeriver)
			{
				FMLClientHandler.instance().getClient().renderEngine.func_110577_a(RenderCoercionDeriver.TEXTURE_ON);
				GL11.glTranslated(0.5, 1.9, 0.5);
				GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
				GL11.glScalef(1.3f, 1.3f, 1.3f);
				RenderCoercionDeriver.MODEL.render(0, 0.0625F);
			}
			else if (block instanceof BlockForceManipulator)
			{
				FMLClientHandler.instance().getClient().renderEngine.func_110577_a(RenderForceManipulator.TEXTURE_ON);
				GL11.glTranslated(0.5, 1.4, 0.5);
				GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
				RenderForceManipulator.MODEL.render(0.0625F);
			}

			GL11.glPopMatrix();
		}
		else
		{
			CalclaviaRenderHelper.renderNormalBlockAsItem(block, metadata, renderer);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess iBlockAccess, int x, int y, int z, Block block, int modelID, RenderBlocks renderer)
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
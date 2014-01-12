/**
 * 
 */
package resonantinduction.electrical.render;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import resonantinduction.electrical.armbot.BlockArmbot;
import resonantinduction.electrical.armbot.RenderArmbot;
import resonantinduction.electrical.battery.BlockBattery;
import resonantinduction.electrical.battery.RenderBattery;
import resonantinduction.electrical.generator.solar.BlockSolarPanel;
import resonantinduction.electrical.generator.solar.RenderSolarPanel;
import resonantinduction.electrical.levitator.BlockLevitator;
import resonantinduction.electrical.levitator.RenderLevitator;
import resonantinduction.electrical.tesla.BlockTesla;
import resonantinduction.electrical.tesla.RenderTesla;
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
public class ElectricalBlockRenderingHandler implements ISimpleBlockRenderingHandler
{
	public static final ElectricalBlockRenderingHandler INSTANCE = new ElectricalBlockRenderingHandler();
	public static final int ID = RenderingRegistry.getNextAvailableRenderId();

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
			for (int i = 2; i < 6; i++)
			{
				glPushMatrix();
				glTranslatef(0.5f, 0, 0.5f);
				GL11.glRotatef(90 * i, 0, 1, 0);
				glScalef(0.5f, 0.5f, 0.5f);
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderBattery.TEXTURE_CAP);
				RenderBattery.MODEL.renderAll();
				glPopMatrix();
			}
		}
		else if (block instanceof BlockSolarPanel)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderSolarPanel.TEXTURE);
			GL11.glTranslatef(0.0F, 1.1F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			RenderSolarPanel.MODEL.render(0.0625F);
		}
		else if (block instanceof BlockArmbot)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderArmbot.TEXTURE);
			GL11.glTranslatef(0.0F, 0.7F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			GL11.glScalef(0.8f, 0.8f, 0.8f);
			RenderArmbot.MODEL.render(0.0625F, 0, 0);
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

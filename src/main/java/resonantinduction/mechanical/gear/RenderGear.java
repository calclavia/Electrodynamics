package resonantinduction.mechanical.gear;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGear
{
	public static final RenderGear INSTANCE = new RenderGear();
	public final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "gear/gear.obj");
	public final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "gear/gear.png");

	public void renderInventory(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) 0.5F, (float) 0.5F, (float) 0.5F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		MODEL.renderAll();
		GL11.glPopMatrix();
	}

	public void renderDynamic(PartGear part, int x, int y, int z)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

		switch (part.placementSide)
		{
			case DOWN:
				break;
			case UP:
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glTranslatef(0, -2, 0);
				break;
			case NORTH:
				GL11.glRotatef(90, 1, 0, 0);
				GL11.glTranslatef(0, -1, -1);
				break;
			case SOUTH:
				GL11.glRotatef(-90, 1, 0, 0);
				GL11.glTranslatef(0, -1, 1);
				break;
			case WEST:
				GL11.glRotatef(90, 0, 0, 1);
				GL11.glTranslatef(1, -1, 0);
				break;
			case EAST:
				GL11.glRotatef(-90, 0, 0, 1);
				GL11.glTranslatef(-1, -1, 0);
				break;
		}
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		MODEL.renderAll();
		GL11.glPopMatrix();
	}
}
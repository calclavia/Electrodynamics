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
		GL11.glTranslatef(-0.3f, 0.6f, 0);
		GL11.glRotatef(75, 1, 0, 0);
		GL11.glScalef(10, 10, 10);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		MODEL.renderAll();
		GL11.glPopMatrix();
	}

	public void renderDynamic(PartGear part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		// Center the model first.
		GL11.glTranslatef((float) x, (float) y + 0.5f, (float) z);
		GL11.glPushMatrix();

		switch (part.placementSide)
		{
			case DOWN:
				GL11.glTranslatef(0, -0.5f, 0);
				break;
			case UP:
				GL11.glTranslatef(0, 0.5f, 0);
				break;
			case NORTH:
				GL11.glTranslatef(0, 0.5f, 0);
				GL11.glRotatef(90, 1, 0, 0);
				break;
			case SOUTH:
				GL11.glTranslatef(0, -0.5f, 1f);
				GL11.glRotatef(-90, 1, 0, 0);
				break;
			case WEST:
				GL11.glTranslatef(0.1f, -0.5f, 0);
				GL11.glRotatef(90, 0, 0, 1);
				break;
			case EAST:
				GL11.glTranslatef(0.9f, 0.5f, 0);
				GL11.glRotatef(-90, 0, 0, 1);
				break;
		}

		GL11.glScalef(10, 10, 10);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		MODEL.renderAll();

		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}
}
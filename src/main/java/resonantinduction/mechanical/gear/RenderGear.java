package resonantinduction.mechanical.gear;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import calclavia.lib.render.RenderUtility;
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
		GL11.glRotatef(90, 1, 0, 0);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		MODEL.renderAll();
	}

	public void renderDynamic(PartGear part, double x, double y, double z, float frame)
	{
		GL11.glPushMatrix();
		// Center the model first.
		GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
		GL11.glPushMatrix();

		RenderUtility.rotateFaceBlockToSide(part.placementSide);

		GL11.glRotatef((float) Math.toDegrees(part.angle), 0, 1, 0);

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		MODEL.renderAll();
		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}
}
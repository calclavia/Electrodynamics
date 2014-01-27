package resonantinduction.electrical.transformer;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTransformer
{
	public static final RenderTransformer INSTANCE = new RenderTransformer();

	public static final WavefrontObject MODEL = (WavefrontObject) AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "transformer.obj");
	public static final ResourceLocation TEXTURE_COIL = new ResourceLocation(Reference.DOMAIN, Reference.BLOCK_TEXTURE_DIRECTORY + "models/wire.png");
	public static final ResourceLocation TEXTURE_STONE = new ResourceLocation(Reference.BLOCK_TEXTURE_DIRECTORY + "stone.png");
	public static final ResourceLocation TEXTURE_IRON = new ResourceLocation(Reference.BLOCK_TEXTURE_DIRECTORY + "iron_block.png");

	public void doRender()
	{
		GL11.glScalef(0.5f, 0.5f, 0.5f);

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_COIL);
		GL11.glColor4f(0.72f, .45f, 0.2f, 1);
		MODEL.renderOnly("InsulatorLayerHigh", "InsulatorLayerMed", "InsulatorLayerLow");
		MODEL.renderOnly("OuterWindingConnector", "OuterWindingHigh", "OuterWindingMed", "OuterWindingLow");
		MODEL.renderOnly("InnerWindingConnector", "InnerWindingHigh", "InnerWindingMed", "InnerWindingLow");

		GL11.glColor4f(1, 1, 1, 1);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_IRON);
		MODEL.renderOnly("core");
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_STONE);
		MODEL.renderOnly("base");
	}

	public void render(PartTransformer part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		RenderUtility.rotateFaceBlockToSide(part.placementSide);
		GL11.glRotatef(90, 0, 1, 0);
		RenderUtility.rotateBlockBasedOnDirection(part.getFacing());
		doRender();
		GL11.glPopMatrix();
	}
}
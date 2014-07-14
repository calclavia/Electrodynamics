package resonantinduction.electrical.transformer;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTransformer implements ISimpleItemRenderer
{
	public static final RenderTransformer INSTANCE = new RenderTransformer();

	public static final WavefrontObject MODEL = (WavefrontObject) AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "transformer.obj");
	public static final ResourceLocation TEXTURE_COIL = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "transformer_winding.png");
	public static final ResourceLocation TEXTURE_STONE = new ResourceLocation(Reference.BLOCK_TEXTURE_DIRECTORY + "stone.png");
	public static final ResourceLocation TEXTURE_IRON = new ResourceLocation(Reference.BLOCK_TEXTURE_DIRECTORY + "iron_block.png");

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glTranslated(0, -0.2f, 0);
		doRender();
	}

	public void doRender()
	{
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_COIL);
		MODEL.renderAllExcept("core", "base");
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
		RenderUtility.rotateBlockBasedOnDirection(part.getFacing());
		GL11.glRotatef(90, 0, 1, 0);
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_COIL);

		switch (part.multiplier)
		{
			case 0:
				MODEL.renderOnly("InsulatorLayerLow", "OuterWindingLowBox", "InnerWindingLowBox");
				break;
			case 1:
				MODEL.renderOnly("InsulatorLayerMed", "OuterWindingMedBox", "InnerWindingMedBox");
				break;
			case 2:
				MODEL.renderOnly("InnerWindingHighBox", "InsulatorLayerHigh", "OuterWindingHighBox");
				break;
		}

		MODEL.renderOnly("OuterWindingConnector", "InnerWindingConnector");

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_IRON);
		MODEL.renderOnly("core");
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_STONE);
		MODEL.renderOnly("base");
		GL11.glPopMatrix();
	}

}
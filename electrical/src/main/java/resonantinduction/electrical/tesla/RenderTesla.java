package resonantinduction.electrical.tesla;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderTesla extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE_BOTTOM = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "tesla_bottom.png");
	public static final ResourceLocation TEXTURE_MIDDLE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "tesla_middle.png");
	public static final ResourceLocation TEXTURE_TOP = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "tesla_top.png");

	public static final IModelCustom MODEL_BOTTOM = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "teslaBottom.tcn");
	public static final IModelCustom MODEL_MIDDLE = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "teslaMiddle.tcn");
	public static final IModelCustom MODEL_TOP = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "teslaTop.tcn");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		int meta = t.getBlockType() != null ? t.getBlockMetadata() : 0;

		switch (meta)
		{
			default:
				bindTexture(TEXTURE_BOTTOM);
				MODEL_BOTTOM.renderAll();
				break;
			case 1:
				bindTexture(TEXTURE_MIDDLE);
				MODEL_MIDDLE.renderAll();
				break;
			case 2:
				bindTexture(TEXTURE_TOP);
				MODEL_TOP.renderAll();
				break;
		}

		GL11.glPopMatrix();
	}

}

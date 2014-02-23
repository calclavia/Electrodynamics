package resonantinduction.mechanical.fluid.transport;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;

public class RenderPump extends TileEntitySpecialRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "pump.tcn");
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pump.png");

	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		TilePump tile = (TilePump) tileEntity;

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		GL11.glRotatef(90, 0, 1, 0);
		if (tile.worldObj != null)
			RenderUtility.rotateBlockBasedOnDirection(tile.getDirection());

		bindTexture(TEXTURE);
		MODEL.renderAll();
		GL11.glPopMatrix();
	}
}

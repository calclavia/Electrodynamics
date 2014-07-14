package resonantinduction.mechanical.fluid.transport;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;

public class RenderPump extends TileEntitySpecialRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "pump.tcn");
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pump.png");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		TilePump tile = (TilePump) tileEntity;

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		GL11.glRotatef(-90, 0, 1, 0);

		if (tile.worldObj != null)
			RenderUtility.rotateBlockBasedOnDirection(tile.getDirection());

		bindTexture(TEXTURE);

		List<String> notRendered = new ArrayList<String>();

		GL11.glPushMatrix();
		GL11.glRotated(Math.toDegrees((float) tile.mechanicalNode.renderAngle), 0, 0, 1);

		for (int i = 1; i <= 12; i++)
		{
			String fin = "fin" + i;
			String innerFin = "innerFin" + i;
			notRendered.add(fin);
			notRendered.add(innerFin);
			MODEL.renderOnly(fin, innerFin);
		}

		GL11.glPopMatrix();

		MODEL.renderAllExcept(notRendered.toArray(new String[0]));
		GL11.glPopMatrix();
	}
}

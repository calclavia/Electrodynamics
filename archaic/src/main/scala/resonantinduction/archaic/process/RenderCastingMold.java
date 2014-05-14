package resonantinduction.archaic.process;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonant.lib.render.RenderItemOverlayUtility;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCastingMold extends TileEntitySpecialRenderer
{
	public static RenderCastingMold INSTANCE = new RenderCastingMold();
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "castingMold.tcn");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileCastingMold)
		{
			TileCastingMold tile = (TileCastingMold) tileEntity;

			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
			GL11.glTranslated(0, -0.25, 0);
			GL11.glScalef(0.5f, 0.5f, 0.5f);
			RenderUtility.bind(Reference.DOMAIN, Reference.MODEL_PATH + "castingMold.png");
			MODEL.renderAll();
			GL11.glPopMatrix();

			if (tile.worldObj != null)
				RenderItemOverlayUtility.renderItemOnSides(tileEntity, tile.getStackInSlot(0), x, y, z, "");
		}
	}
}

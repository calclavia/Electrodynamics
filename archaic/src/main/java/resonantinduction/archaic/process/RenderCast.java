package resonantinduction.archaic.process;

import org.lwjgl.opengl.GL11;

import calclavia.lib.render.RenderUtility;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RenderItemOverlayTile;
import resonantinduction.electrical.battery.RenderBattery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCast extends RenderItemOverlayTile
{
	public static RenderCast INSTANCE = new RenderCast();
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "cast.tcn");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileCast)
		{
			TileCast tile = (TileCast) tileEntity;

			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
			GL11.glTranslated(0.25, -0.25, -0.25);
			GL11.glScalef(0.5f, 0.5f, 0.5f);
			RenderUtility.bind(Reference.DOMAIN, Reference.MODEL_PATH + "cast.png");
			MODEL.renderAll();
			GL11.glPopMatrix();

			if (tile.worldObj != null)
				renderItemOnSides(tileEntity, tile.getStackInSlot(0), x, y, z, "");
		}
	}
}

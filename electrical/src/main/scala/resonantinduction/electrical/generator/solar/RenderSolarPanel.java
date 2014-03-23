package resonantinduction.electrical.generator.solar;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.electrical.Electrical;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.utility.WorldUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSolarPanel extends TileEntitySpecialRenderer
{
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		float width = 0.25f;
		float thickness = 0.07f;

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.05f, z + 0.5);
		RenderUtility.bind(TextureMap.locationBlocksTexture);

		// Render the main panel
		RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, width, 0.5, Electrical.blockSolarPanel);
		ForgeDirection dir = ForgeDirection.DOWN;

		// Render edges
		for (int i = 2; i < 6; i++)
		{
			ForgeDirection check = ForgeDirection.getOrientation(i);

			if (tile.worldObj == null || !(tile.worldObj.getBlockTileEntity(tile.xCoord + check.offsetX, tile.yCoord + check.offsetY, tile.zCoord + check.offsetZ) instanceof TileSolarPanel))
			{
				GL11.glPushMatrix();
				GL11.glRotatef(WorldUtility.getAngleFromForgeDirection(check), 0, 1, 0);
				RenderUtility.renderCube(0.5 - thickness, -0.0501, -0.501, 0.501, width + 0.001, 0.501, Electrical.blockSolarPanel, Electrical.blockSolarPanel.sideIcon);
				GL11.glPopMatrix();
			}
		}

		GL11.glPopMatrix();
	}
}
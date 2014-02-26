package resonantinduction.archaic.piston;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPiston extends TileEntitySpecialRenderer
{
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "piston.png");

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		// Angle in radians of the rotor.
		float angle = 0;
		float radius = 0;
		// Length of piston arm
		float length = 0.8f;

		double beta = Math.asin((radius * Math.sin(angle)) / length);

		/**
		 * Render Piston Arm
		 */
		GL11.glPushMatrix();
		double pistonTranslateX = 2 * length * Math.cos(beta);
		double pistonTranslateY = 2 * length * Math.sin(beta);

		GL11.glTranslated(pistonTranslateX, 0, pistonTranslateY);
		GL11.glRotated(Math.toDegrees(beta), 0, 0, 1);
		GL11.glPopMatrix();

		/**
		 * Render Piston Rotor
		 */
		GL11.glPushMatrix();
		GL11.glRotated(Math.toDegrees(angle), 0, 0, 1);
		GL11.glPopMatrix();

		GL11.glPopMatrix();
	}
}
package resonantinduction.mechanical.process.crusher;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMechanicalPiston extends TileEntitySpecialRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "rejector.tcn");
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "rejector.png");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		TileMechanicalPiston tile = (TileMechanicalPiston) tileEntity;
		GL11.glRotated(-90, 0, 1, 0);

		if (tile.worldObj != null)
			RenderUtility.rotateBlockBasedOnDirection(tile.getDirection());

		RenderUtility.bind(TEXTURE);

		// Angle in radians of the rotor.
		float angle = tile.angle;
		float radius = 0.5f;
		// Length of piston arm
		float length = 1f;

		double beta = Math.asin((radius * Math.sin(angle)) / (length/2));

		/**
		 * Render Piston Rod
		 */
		GL11.glPushMatrix();
		double pistonTranslateX = 2 * length * Math.cos(beta);
		double pistonTranslateY = 2 * length * Math.sin(beta);

		GL11.glTranslated(0, pistonTranslateY, pistonTranslateX);
		GL11.glRotated(-Math.toDegrees(beta), 1, 0, 0);
		//MODEL.renderOnly("PistonShaft", "PistonFace", "PistonFace2");
		GL11.glPopMatrix();

		/**
		 * Render Piston Rotor
		 */
		GL11.glPushMatrix();
		GL11.glTranslated(0, 0, (0.5 * Math.cos(angle - Math.PI)) - 0.5);
		MODEL.renderOnly("PistonShaft", "PistonFace", "PistonFace2");
		GL11.glPopMatrix();

		MODEL.renderAllExcept("PistonShaft", "PistonFace", "PistonFace2");
		GL11.glPopMatrix();
	}
}
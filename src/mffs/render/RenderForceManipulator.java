package mffs.render;

import mffs.ModularForceFieldSystem;
import mffs.base.TileEntityBase;
import mffs.render.model.ModelForceManipulator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderForceManipulator extends TileEntitySpecialRenderer
{
	public static final String TEXTURE_ON = "forceManipulator_on.png";
	public static final String TEXTURE_OFF = "forceManipulator_off.png";
	public static final ModelForceManipulator MODEL = new ModelForceManipulator();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileEntityBase tileEntity = (TileEntityBase) t;

		/**
		 * Render Model
		 */
		if (tileEntity.isActive())
		{
			this.bindTextureByName(ModularForceFieldSystem.MODEL_DIRECTORY + TEXTURE_ON);
		}
		else
		{
			this.bindTextureByName(ModularForceFieldSystem.MODEL_DIRECTORY + TEXTURE_OFF);
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

		MODEL.render(0.0625F);

		GL11.glPopMatrix();
	}
}
package mffs.mobilize;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.ModularForceFieldSystem;
import mffs.render.model.ModelForceManipulator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderForceManipulator extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE_ON = new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.MODEL_DIRECTORY + "forceManipulator_on.png");
	public static final ResourceLocation TEXTURE_OFF = new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.MODEL_DIRECTORY + "forceManipulator_off.png");

	public static final ModelForceManipulator MODEL = new ModelForceManipulator();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileMFFS tileEntity = (TileMFFS) t;

		/**
		 * Render Model
		 */
		if (tileEntity.isActive())
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_ON);
		}
		else
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_OFF);
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1F);

		// Rotate the model
		switch (tileEntity.getDirection())
		{
			case UP:
				GL11.glRotatef(-90, 1f, 0f, 0);
				GL11.glTranslated(0, -1, 1);
				break;
			case DOWN:
				GL11.glRotatef(90, 1f, 0f, 0);
				GL11.glTranslated(0, -1, -1);
				break;
			case NORTH:
				GL11.glRotatef(0, 0f, 1f, 0f);
				break;
			case SOUTH:
				GL11.glRotatef(180, 0f, 1f, 0f);
				break;
			case WEST:
				GL11.glRotatef(-90, 0f, 1f, 0f);
				break;
			case EAST:
				GL11.glRotatef(90, 0f, 1f, 0f);
				break;
			default:
				break;
		}

		MODEL.render(0.0625F);

		GL11.glPopMatrix();
	}
}
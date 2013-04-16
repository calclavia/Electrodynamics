package mffs.render;

import mffs.ModularForceFieldSystem;
import mffs.render.model.ModelForceFieldProjector;
import mffs.tileentity.TileEntityForceFieldProjector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class RenderForceFieldProjector extends TileEntitySpecialRenderer
{
	public static final String TEXTURE_ON = "projector_on.png";
	public static final String TEXTURE_OFF = "projector_off.png";
	public static final ModelForceFieldProjector MODEL = new ModelForceFieldProjector();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		if (t instanceof TileEntityForceFieldProjector)
		{
			TileEntityForceFieldProjector tileEntity = (TileEntityForceFieldProjector) t;

			/**
			 * Render Model
			 */
			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);

			if (tileEntity.isActive())
			{
				this.bindTextureByName(ModularForceFieldSystem.MODEL_DIRECTORY + TEXTURE_ON);
			}
			else
			{
				this.bindTextureByName(ModularForceFieldSystem.MODEL_DIRECTORY + TEXTURE_OFF);
			}

			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

			MODEL.render(tileEntity.animation, 0.0625F);

			GL11.glPopMatrix();

			if (tileEntity.getMode() != null)
			{
				/**
				 * Render Projection
				 */
				Tessellator tessellator = Tessellator.instance;

				RenderHelper.disableStandardItemLighting();
				GL11.glPushMatrix();
				GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

				double xDifference = Minecraft.getMinecraft().thePlayer.posX - (tileEntity.xCoord + 0.5);
				double zDifference = Minecraft.getMinecraft().thePlayer.posZ - (tileEntity.zCoord + 0.5);
				float rotatation = (float) Math.toDegrees(Math.atan2(zDifference, xDifference));
				GL11.glRotatef(-rotatation + 27, 0.0F, 1.0F, 0.0F);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glDepthMask(false);
				GL11.glPushMatrix();

				tessellator.startDrawing(6);
				float height = 2;
				float width = 2;
				tessellator.setColorRGBA(72, 198, 255, 255);
				tessellator.addVertex(0.0D, 0.0D, 0.0D);
				tessellator.setColorRGBA_I(0, 0);
				tessellator.addVertex(-0.866D * width, height, -0.5F * width);
				tessellator.addVertex(0.866D * width, height, -0.5F * width);
				tessellator.addVertex(0.0D, height, 1.0F * width);
				tessellator.addVertex(-0.866D * width, height, -0.5F * width);
				tessellator.draw();

				GL11.glPopMatrix();
				GL11.glDepthMask(true);
				GL11.glDisable(GL11.GL_CULL_FACE);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glShadeModel(GL11.GL_FLAT);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				RenderHelper.enableStandardItemLighting();
				GL11.glPopMatrix();

				/**
				 * Render Hologram
				 */
				GL11.glPushMatrix();
				GL11.glTranslated(x + 0.5, y + 1.35, z + 0.5);
				this.bindTextureByName(ModularForceFieldSystem.MODEL_DIRECTORY + "force_cube.png");

				// Enable Blending
				GL11.glShadeModel(GL11.GL_SMOOTH);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				// Disable Lighting/Glow On
				RenderHelper.disableStandardItemLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

				GL11.glPushMatrix();
				GL11.glColor4f(1, 1, 1, (float) Math.sin((double) tileEntity.getTicks() / 10) / 2 + 1);
				GL11.glTranslatef(0, (float) Math.sin(Math.toRadians(tileEntity.getTicks() * 3)) / 7, 0);
				GL11.glRotatef(tileEntity.getTicks() * 4, 0, 1, 0);
				GL11.glRotatef(36f + tileEntity.getTicks() * 4, 0, 1, 1);
				tileEntity.getMode().render(tileEntity, x, y, z, f, tileEntity.getTicks());
				GL11.glPopMatrix();

				// Enable Lighting/Glow Off
				RenderHelper.enableStandardItemLighting();

				// Disable Blending
				GL11.glShadeModel(GL11.GL_FLAT);
				GL11.glDisable(GL11.GL_LINE_SMOOTH);
				GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
				GL11.glDisable(GL11.GL_BLEND);

				GL11.glPopMatrix();
			}
		}
	}
}
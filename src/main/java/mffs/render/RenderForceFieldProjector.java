package mffs.render;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.render.model.ModelForceFieldProjector;
import mffs.tile.TileForceFieldProjector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonant.lib.render.RenderUtility;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderForceFieldProjector extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE_ON = new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.MODEL_DIRECTORY + "projector_on.png");
	public static final ResourceLocation TEXTURE_OFF = new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.MODEL_DIRECTORY + "projector_off.png");
	public static final ResourceLocation FORCE_CUBE = new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.MODEL_DIRECTORY + "force_cube.png");

	public static final ModelForceFieldProjector MODEL = new ModelForceFieldProjector();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		if (t instanceof TileForceFieldProjector)
		{
			TileForceFieldProjector tileEntity = (TileForceFieldProjector) t;

			/**
			 * Render Model
			 */
			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);

			if (tileEntity.isActive())
			{
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_ON);
			}
			else
			{
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_OFF);
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

				if (Settings.HIGH_GRAPHICS)
				{
					/**
					 * Render Hologram
					 */
					GL11.glPushMatrix();
					GL11.glTranslated(x + 0.5, y + 1.35, z + 0.5);
					FMLClientHandler.instance().getClient().renderEngine.bindTexture(FORCE_CUBE);

					// Enable Blending
					RenderUtility.enableBlending();

					// Disable Lighting/Glow On
					RenderUtility.disableLighting();

					GL11.glPushMatrix();
					GL11.glColor4f(1, 1, 1, (float) Math.sin((double) tileEntity.getTicks() / 10) / 2 + 1);
					GL11.glTranslatef(0, (float) Math.sin(Math.toRadians(tileEntity.getTicks() * 3)) / 7, 0);
					GL11.glRotatef(tileEntity.getTicks() * 4, 0, 1, 0);
					GL11.glRotatef(36f + tileEntity.getTicks() * 4, 0, 1, 1);
					tileEntity.getMode().render(tileEntity, x, y, z, f, tileEntity.getTicks());
					GL11.glPopMatrix();

					// Enable Lighting/Glow Off
					RenderUtility.enableLighting();

					// Disable Blending
					RenderUtility.disableBlending();

					GL11.glPopMatrix();
				}
			}
		}
	}
}
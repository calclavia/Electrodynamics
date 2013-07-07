package mffs.render;

import mffs.ModularForceFieldSystem;
import mffs.base.TileEntityBase;
import mffs.render.model.ModelFortronCapacitor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFortronCapacitor extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE_ON = new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.MODEL_DIRECTORY + "coercionDeriver_on.png");
	public static final ResourceLocation TEXTURE_OFF = new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.MODEL_DIRECTORY + "coercionDeriver_off.png");

	public static final ModelFortronCapacitor MODEL = new ModelFortronCapacitor();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileEntityBase tileEntity = (TileEntityBase) t;

		/**
		 * Render Model
		 */
		if (tileEntity.isActive())
		{
			FMLClientHandler.instance().getClient().renderEngine.func_110577_a(TEXTURE_ON);
		}
		else
		{
			FMLClientHandler.instance().getClient().renderEngine.func_110577_a(TEXTURE_OFF);
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.95, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		GL11.glScalef(1.3f, 1.3f, 1.3f);

		MODEL.render(0.0625F);

		GL11.glPopMatrix();
	}
}
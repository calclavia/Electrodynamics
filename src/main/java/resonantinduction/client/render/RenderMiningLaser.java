package resonantinduction.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.client.model.ModelLaserTile;
import resonantinduction.mechanics.machine.mining.TileMiningLaser;
import resonantinduction.transport.ResonantInductionTransport;

/** @author Darkguardsman */
public class RenderMiningLaser extends TileEntitySpecialRenderer
{

	public static final ModelLaserTile model = new ModelLaserTile();
	public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "LaserTile.png");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double d, double d1, double d2, float f)
	{
		bindTexture(TEXTURE);

		GL11.glPushMatrix();
		GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		if (tileEntity instanceof TileMiningLaser)
		{
			float yaw = ((TileMiningLaser) tileEntity).getYaw() - 90;
			GL11.glRotatef(yaw, 0, 1, 0);
		}
		model.renderAll();
		GL11.glPopMatrix(); // end
	}
}

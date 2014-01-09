package resonantinduction.old.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.old.Reference;
import resonantinduction.old.client.model.ModelTestCar;
import resonantinduction.old.transport.vechicle.EntityAdvanced;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTestCar extends Render
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "TestCar.png");

	/** instance of ModelBoat for rendering */
	protected ModelBase modelBoat;

	public RenderTestCar()
	{
		this.shadowSize = 0.0F;
		this.modelBoat = new ModelTestCar();
	}

	@Override
	public void doRender(Entity entity, double rx, double ry, double rz, float rYaw, float rPitch)
	{

		GL11.glPushMatrix();
		GL11.glTranslatef((float) rx, (float) ry + 1.2f, (float) rz);
		GL11.glRotatef(180.0F - rYaw, 0.0F, 1.0F, 0.0F);
		if (entity instanceof EntityAdvanced)
		{
			float f2 = ((EntityAdvanced) entity).getTimeSinceHit() - rPitch;
			float f3 = ((EntityAdvanced) entity).getHealth() - rPitch;

			if (f3 < 0.0F)
			{
				f3 = 0.0F;
			}

			if (f2 > 0.0F)
			{
				GL11.glRotatef(MathHelper.sin(f2) * f2 * f3 / 10.0F * ((EntityAdvanced) entity).getForwardDirection(), 1.0F, 0.0F, 0.0F);
			}
		}

		float f4 = 0.75F;
		GL11.glScalef(f4, f4, f4);
		GL11.glScalef(1.0F / f4, 1.0F / f4, 1.0F / f4);
		this.bindEntityTexture(entity);
		GL11.glScalef(-1.0F, -1.0F, 1.0F);
		this.modelBoat.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity par1Entity)
	{
		return TEXTURE;
	}
}

package resonantinduction.client.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import resonantinduction.client.model.ModelTurkey;
import resonantinduction.misc.EntityTurkey;
import resonantinduction.transport.ResonantInductionTransport;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTurkey extends RenderLiving
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "Turkey.png");

	public RenderTurkey()
	{
		super(new ModelTurkey(), 0.3f);
	}

	public void renderChicken(EntityTurkey par1EntityChicken, double par2, double par4, double par6, float par8, float par9)
	{
		super.doRenderLiving(par1EntityChicken, par2, par4, par6, par8, par9);
	}

	protected ResourceLocation getChickenTextures(EntityTurkey par1EntityChicken)
	{
		return TEXTURE;
	}

	protected float getWingRotation(EntityTurkey par1EntityChicken, float par2)
	{
		float f1 = par1EntityChicken.field_70888_h + (par1EntityChicken.field_70886_e - par1EntityChicken.field_70888_h) * par2;
		float f2 = par1EntityChicken.field_70884_g + (par1EntityChicken.destPos - par1EntityChicken.field_70884_g) * par2;
		return (MathHelper.sin(f1) + 1.0F) * f2;
	}

	@Override
	public void doRenderLiving(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9)
	{
		this.renderChicken((EntityTurkey) par1EntityLiving, par2, par4, par6, par8, par9);
	}

	/** Defines what float the third param in setRotationAngles of ModelBase is */
	@Override
	protected float handleRotationFloat(EntityLivingBase par1EntityLivingBase, float par2)
	{
		return this.getWingRotation((EntityTurkey) par1EntityLivingBase, par2);
	}

	@Override
	public void renderPlayer(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6, float par8, float par9)
	{
		this.renderChicken((EntityTurkey) par1EntityLivingBase, par2, par4, par6, par8, par9);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call
	 * Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(Entity par1Entity)
	{
		return this.getChickenTextures((EntityTurkey) par1Entity);
	}

	/**
	 * Actually renders the given argument. This is a synthetic bridge method, always casting down
	 * its argument and then handing it off to a worker function which does the actual work. In all
	 * probabilty, the class Render is generic (Render<T extends Entity) and this method has
	 * signature public void doRender(T entity, double d, double d1, double d2, float f, float f1).
	 * But JAD is pre 1.5 so doesn't do that.
	 */
	@Override
	public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
	{
		this.renderChicken((EntityTurkey) par1Entity, par2, par4, par6, par8, par9);
	}
}

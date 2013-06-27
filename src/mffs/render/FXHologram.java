package mffs.render;

import mffs.ModularForceFieldSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;
import calclavia.lib.render.CalclaviaRenderHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FXHologram extends EntityFX
{
	private Vector3 targetPosition = null;

	public FXHologram(World par1World, Vector3 position, float red, float green, float blue, int age)
	{
		super(par1World, position.x, position.y, position.z);
		this.setRBGColorF(red, green, blue);
		this.particleMaxAge = age;
		this.noClip = true;
	}

	public FXHologram setTarget(Vector3 targetPosition)
	{
		this.targetPosition = targetPosition;
		this.motionX = (this.targetPosition.x - this.posX) / this.particleMaxAge;
		this.motionY = (this.targetPosition.y - this.posY) / this.particleMaxAge;
		this.motionZ = (this.targetPosition.z - this.posZ) / this.particleMaxAge;

		return this;
	}

	@Override
	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge)
		{
			this.setDead();
			return;
		}

		if (this.targetPosition != null)
		{
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
		}
	}

	@Override
	public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5)
	{
		tessellator.draw();

		GL11.glPushMatrix();
		float xx = (float) (this.prevPosX + (this.posX - this.prevPosX) * f - interpPosX);
		float yy = (float) (this.prevPosY + (this.posY - this.prevPosY) * f - interpPosY);
		float zz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * f - interpPosZ);
		GL11.glTranslated(xx, yy, zz);
		GL11.glScalef(1.01f, 1.01f, 1.01f);

		float op = 0.5f;

		if ((this.particleMaxAge - this.particleAge <= 4))
		{
			op = 0.5f - (5 - (this.particleMaxAge - this.particleAge)) * 0.1F;
		}

		GL11.glColor4d(this.particleRed, this.particleGreen, this.particleBlue, op * 2);

		CalclaviaRenderHelper.disableLighting();
		CalclaviaRenderHelper.enableBlending();
		Minecraft.getMinecraft().renderEngine.bindTexture("/terrain.png");
		CalclaviaRenderHelper.renderNormalBlockAsItem(ModularForceFieldSystem.blockForceField, 0, new RenderBlocks());
		CalclaviaRenderHelper.disableBlending();
		// CalclaviaRenderHelper.enableLighting();
		GL11.glPopMatrix();

		tessellator.startDrawingQuads();
		Minecraft.getMinecraft().renderEngine.bindTexture("/particles.png");
	}
}

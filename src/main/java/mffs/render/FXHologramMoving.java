package mffs.render;

import mffs.ModularForceFieldSystem;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.CalclaviaRenderHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FXHologramMoving extends EntityFX
{
	public FXHologramMoving(World par1World, Vector3 position, float red, float green, float blue, int age)
	{
		super(par1World, position.x, position.y, position.z);
		this.setRBGColorF(red, green, blue);
		this.particleMaxAge = age;
		this.noClip = true;
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

		double completion = (double) this.particleAge / (double) this.particleMaxAge;
		GL11.glTranslated(0, (completion - 1) / 2, 0);
		GL11.glScaled(1, completion, 1);

		float op = 0.5f;

		if ((this.particleMaxAge - this.particleAge <= 4))
		{
			op = 0.5f - (5 - (this.particleMaxAge - this.particleAge)) * 0.1F;
		}

		GL11.glColor4d(this.particleRed, this.particleGreen, this.particleBlue, op * 2);

		CalclaviaRenderHelper.disableLighting();
		CalclaviaRenderHelper.enableBlending();
		CalclaviaRenderHelper.setTerrainTexture();
		CalclaviaRenderHelper.renderNormalBlockAsItem(ModularForceFieldSystem.blockForceField, 0, new RenderBlocks());
		CalclaviaRenderHelper.disableBlending();
		CalclaviaRenderHelper.enableLighting();
		GL11.glPopMatrix();

		tessellator.startDrawingQuads();
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(CalclaviaRenderHelper.PARTICLE_RESOURCE);
	}
}

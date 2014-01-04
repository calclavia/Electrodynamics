package mffs.render;

import mffs.ModularForceFieldSystem;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.CalclaviaRenderHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class FXHologramOrbit extends FXHologram
{
	private Vector3 orbitPosition;
	private float maxSpeed;
	private double rotation = 0;

	public FXHologramOrbit(World par1World, Vector3 orbitPosition, Vector3 position, float red, float green, float blue, int age, float maxSpeed)
	{
		super(par1World, position, red, green, blue, age);
		this.orbitPosition = orbitPosition;
		this.maxSpeed = maxSpeed;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		double xDifference = this.posX - orbitPosition.x;
		double yDifference = this.posY - orbitPosition.y;
		double zDifference = this.posZ - orbitPosition.z;

		double speed = this.maxSpeed * ((float) this.particleAge / (float) this.particleMaxAge);
		Vector3 originalPosition = new Vector3(this);
		Vector3 relativePosition = originalPosition.clone().subtract(this.orbitPosition);
		relativePosition.rotate(speed, 0, 0);
		Vector3 newPosition = this.orbitPosition.clone().add(relativePosition);
		this.rotation += speed;

		// Orbit
		this.moveEntity(newPosition.x - originalPosition.x, newPosition.y - originalPosition.y, newPosition.z - originalPosition.z);
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

		GL11.glRotated(-this.rotation, 0, 1, 0);

		float op = 0.5f;

		if ((this.particleMaxAge - this.particleAge <= 4))
		{
			op = 0.5f - (5 - (this.particleMaxAge - this.particleAge)) * 0.1F;
		}

		GL11.glColor4d(this.particleRed, this.particleGreen, this.particleBlue, op * 2);

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		CalclaviaRenderHelper.enableBlending();
		CalclaviaRenderHelper.setTerrainTexture();
		CalclaviaRenderHelper.renderNormalBlockAsItem(ModularForceFieldSystem.blockForceField, 0, new RenderBlocks());
		CalclaviaRenderHelper.disableBlending();
		GL11.glPopMatrix();

		tessellator.startDrawingQuads();
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(CalclaviaRenderHelper.PARTICLE_RESOURCE);
	}
}

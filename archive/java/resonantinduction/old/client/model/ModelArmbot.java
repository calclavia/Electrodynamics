package resonantinduction.old.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelArmbot extends ModelBase
{
	// fields
	ModelRenderer baseTop;
	ModelRenderer base;
	ModelRenderer armMountRight;
	ModelRenderer armMountLeft;
	ModelRenderer armLower;
	ModelRenderer armLower2;
	ModelRenderer armLower3;
	ModelRenderer armUpper;
	ModelRenderer baseRotation;
	ModelRenderer clampBody;
	ModelRenderer clampBody2;
	ModelRenderer clampClawLower;
	ModelRenderer clampClawLower2;
	ModelRenderer clampClawLower3;

	public ModelArmbot()
	{
		textureWidth = 128;
		textureHeight = 128;

		baseTop = new ModelRenderer(this, 0, 94);
		baseTop.addBox(-6F, 0F, -6F, 12, 3, 12);
		baseTop.setRotationPoint(0F, 18F, 0F);
		baseTop.setTextureSize(64, 32);
		baseTop.mirror = true;
		setRotation(baseTop, 0F, 0F, 0F);
		base = new ModelRenderer(this, 0, 109);
		base.addBox(-8F, 0F, -8F, 16, 3, 16);
		base.setRotationPoint(0F, 21F, 0F);
		base.setTextureSize(64, 32);
		base.mirror = true;
		setRotation(base, 0F, 0F, 0F);
		armMountRight = new ModelRenderer(this, 24, 85);
		armMountRight.addBox(-3.8F, -5F, -2F, 4, 5, 4);
		armMountRight.setRotationPoint(0F, 17F, 0F);
		armMountRight.setTextureSize(128, 128);
		armMountRight.mirror = true;
		setRotation(armMountRight, 0F, 0F, 0F);
		armMountLeft = new ModelRenderer(this, 0, 85);
		armMountLeft.addBox(2F, -5F, -2F, 2, 5, 4);
		armMountLeft.setRotationPoint(0F, 17F, 0F);
		armMountLeft.setTextureSize(64, 32);
		armMountLeft.mirror = true;
		setRotation(armMountLeft, 0F, 0F, 0F);
		armLower = new ModelRenderer(this, 116, 0);
		armLower.addBox(0.3F, -15F, -1.5F, 2, 16, 4);
		armLower.setRotationPoint(0F, 14F, 0F);
		armLower.setTextureSize(64, 32);
		armLower.mirror = true;
		setRotation(armLower, 0.5235988F, 0F, 0F);
		armLower2 = new ModelRenderer(this, 104, 0);
		armLower2.addBox(-2.3F, -15F, -1.5F, 2, 16, 4);
		armLower2.setRotationPoint(0F, 14F, 0F);
		armLower2.setTextureSize(64, 32);
		armLower2.mirror = true;
		setRotation(armLower2, 0.5235988F, 0F, 0F);
		armLower3 = new ModelRenderer(this, 92, 0);
		armLower3.addBox(-1F, -14F, -2F, 2, 14, 4);
		armLower3.setRotationPoint(0F, 14F, 0F);
		armLower3.setTextureSize(64, 32);
		armLower3.mirror = true;
		setRotation(armLower3, 0.5235988F, 0F, 0F);
		armUpper = new ModelRenderer(this, 0, 70);
		armUpper.addBox(-1F, -10F, -1.5F, 2, 12, 3);
		armUpper.setRotationPoint(0F, 2F, -7F);
		armUpper.setTextureSize(64, 32);
		armUpper.mirror = true;
		setRotation(armUpper, 2.513274F, 0F, 0F);
		baseRotation = new ModelRenderer(this, 0, 60);
		baseRotation.addBox(-4.5F, 0F, -4.5F, 9, 1, 9);
		baseRotation.setRotationPoint(0F, 17F, 0F);
		baseRotation.setTextureSize(64, 32);
		baseRotation.mirror = true;
		setRotation(baseRotation, 0F, 0F, 0F);
		clampBody = new ModelRenderer(this, 0, 7);
		clampBody.addBox(-1.5F, -12F, -2.5F, 3, 2, 5);
		clampBody.setRotationPoint(0F, 2F, -7F);
		clampBody.setTextureSize(64, 32);
		clampBody.mirror = true;
		setRotation(clampBody, 2.513274F, 0F, 0F);
		clampBody2 = new ModelRenderer(this, 0, 56);
		clampBody2.addBox(-1F, -14F, -1F, 2, 2, 2);
		clampBody2.setRotationPoint(0F, 2F, -7F);
		clampBody2.setTextureSize(64, 32);
		clampBody2.mirror = true;
		setRotation(clampBody2, 2.513274F, 0F, 0F);
		clampClawLower = new ModelRenderer(this, 0, 25);
		clampClawLower.addBox(-1F, -4F, -1F, 2, 5, 1);
		clampClawLower.setRotationPoint(0F, 13F, -15F);
		clampClawLower.setTextureSize(64, 32);
		clampClawLower.mirror = true;
		setRotation(clampClawLower, 2.9147F, 0F, 0F);
		clampClawLower2 = new ModelRenderer(this, 0, 31);
		clampClawLower2.addBox(-1.2F, -3.5F, 0F, 1, 6, 1);
		clampClawLower2.setRotationPoint(0F, 14F, -16F);
		clampClawLower2.setTextureSize(64, 32);
		clampClawLower2.mirror = true;
		setRotation(clampClawLower2, 2.897247F, 0F, 0F);
		clampClawLower3 = new ModelRenderer(this, 0, 0);
		clampClawLower3.addBox(0.2F, -3.5F, 0F, 1, 6, 1);
		clampClawLower3.setRotationPoint(0F, 14F, -16F);
		clampClawLower3.setTextureSize(64, 32);
		clampClawLower3.mirror = true;
		setRotation(clampClawLower3, 2.897247F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		this.render(f5, entity.rotationYaw, entity.rotationPitch);
	}

	public void render(float f5, float rotationYaw, float rotationPitch)
	{

		/*
		 * armMountRight.setRotationPoint(baseRotation.rotationPointX, armMountRight.rotationPointY,
		 * baseRotation.rotationPointX); armMountLeft.setRotationPoint(baseRotation.rotationPointX,
		 * armMountLeft.rotationPointY, baseRotation.rotationPointX);
		 * armLower.setRotationPoint(baseRotation.rotationPointX, armLower.rotationPointY,
		 * baseRotation.rotationPointX); armUpper.setRotationPoint(baseRotation.rotationPointX,
		 * armUpper.rotationPointY, baseRotation.rotationPointX);
		 * baseRotation.setRotationPoint(baseRotation.rotationPointX, baseRotation.rotationPointY,
		 * baseRotation.rotationPointX); clampBody.setRotationPoint(baseRotation.rotationPointX,
		 * clampBody.rotationPointY, baseRotation.rotationPointX);
		 * clampBody2.setRotationPoint(baseRotation.rotationPointX, clampBody2.rotationPointY,
		 * baseRotation.rotationPointX);
		 * clampClawLower.setRotationPoint(baseRotation.rotationPointX,
		 * clampClawLower.rotationPointY, baseRotation.rotationPointX);
		 * clampClawLower2.setRotationPoint(baseRotation.rotationPointX,
		 * clampClawLower2.rotationPointY, baseRotation.rotationPointX);
		 * armMountRight.rotateAngleY = armBot.rotationYaw; armMountLeft.rotateAngleY =
		 * armBot.rotationYaw; armLower.rotateAngleY = armBot.rotationYaw; armUpper.rotateAngleY =
		 * armBot.rotationYaw; baseRotation.rotateAngleY = armBot.rotationYaw;
		 * clampBody.rotateAngleY = armBot.rotationYaw; clampBody2.rotateAngleY =
		 * armBot.rotationYaw; clampClawLower.rotateAngleY = armBot.rotationYaw;
		 * clampClawLower2.rotateAngleY = armBot.rotationYaw;
		 */

		baseTop.render(f5);
		base.render(f5);
		GL11.glPushMatrix();
		GL11.glRotatef(rotationYaw, 0, 1, 0);
		{
			armMountRight.render(f5);
			armMountLeft.render(f5);
			baseRotation.render(f5);
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(rotationYaw, 0, 1, 0);
		GL11.glTranslatef(0f, 0.9f, 0f);
		GL11.glRotatef(-rotationPitch, 1, 0, 0);
		GL11.glTranslatef(0f, -0.9f, 0f);
		{
			armLower.render(f5);
			GL11.glPushMatrix();
			GL11.glTranslatef(0f, 0.1f, -0.35f);
			GL11.glRotatef(-rotationPitch, 1, 0, 0);
			GL11.glTranslatef(0f, -0.05f, 0.35f);
			{
				armUpper.render(f5);
				clampBody.render(f5);
				clampBody2.render(f5);
				clampClawLower.render(f5);
				clampClawLower2.render(f5);
			}
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}

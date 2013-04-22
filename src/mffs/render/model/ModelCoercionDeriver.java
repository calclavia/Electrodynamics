package mffs.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

import org.lwjgl.opengl.GL11;

public class ModelCoercionDeriver extends ModelBase
{
	// fields
	ModelRenderer corner1;
	ModelRenderer corner2;
	ModelRenderer corner3;
	ModelRenderer corner4;
	ModelRenderer Bout;
	ModelRenderer Baout;
	ModelRenderer Fout;
	ModelRenderer Tout;
	ModelRenderer Core;
	ModelRenderer Movingthingright;
	ModelRenderer Movingthingleft;
	ModelRenderer bottom;

	public ModelCoercionDeriver()
	{
		textureWidth = 64;
		textureHeight = 32;

		corner1 = new ModelRenderer(this, 52, 0);
		corner1.addBox(3F, 16F, 3F, 3, 6, 3);
		corner1.setRotationPoint(0F, 0F, 0F);
		corner1.setTextureSize(64, 32);
		corner1.mirror = true;
		setRotation(corner1, 0F, 0F, 0F);
		corner2 = new ModelRenderer(this, 52, 0);
		corner2.addBox(-6F, 16F, 3F, 3, 6, 3);
		corner2.setRotationPoint(0F, 0F, 0F);
		corner2.setTextureSize(64, 32);
		corner2.mirror = true;
		setRotation(corner2, 0F, 0F, 0F);
		corner3 = new ModelRenderer(this, 52, 0);
		corner3.addBox(-6F, 16F, -6F, 3, 6, 3);
		corner3.setRotationPoint(0F, 0F, 0F);
		corner3.setTextureSize(64, 32);
		corner3.mirror = true;
		setRotation(corner3, 0F, 0F, 0F);
		corner4 = new ModelRenderer(this, 52, 0);
		corner4.addBox(3F, 16F, -6F, 3, 6, 3);
		corner4.setRotationPoint(0F, 0F, 0F);
		corner4.setTextureSize(64, 32);
		corner4.mirror = true;
		setRotation(corner4, 0F, 0F, 0F);
		Bout = new ModelRenderer(this, 24, 19);
		Bout.addBox(-2F, 21F, -2F, 4, 1, 4);
		Bout.setRotationPoint(0F, 0F, 0F);
		Bout.setTextureSize(64, 32);
		Bout.mirror = true;
		setRotation(Bout, 0F, 0F, 0F);
		Baout = new ModelRenderer(this, 24, 14);
		Baout.addBox(-2F, 14F, 3F, 4, 4, 1);
		Baout.setRotationPoint(0F, 2F, 0F);
		Baout.setTextureSize(64, 32);
		Baout.mirror = true;
		setRotation(Baout, 0F, 0F, 0F);
		Fout = new ModelRenderer(this, 24, 14);
		Fout.addBox(-2F, 14F, -4F, 4, 4, 1);
		Fout.setRotationPoint(0F, 2F, 0F);
		Fout.setTextureSize(64, 32);
		Fout.mirror = true;
		setRotation(Fout, 0F, 0F, 0F);
		Tout = new ModelRenderer(this, 24, 19);
		Tout.addBox(-2F, 14F, -2F, 4, 1, 4);
		Tout.setRotationPoint(0F, 0F, 0F);
		Tout.setTextureSize(64, 32);
		Tout.mirror = true;
		setRotation(Tout, 0F, 0F, 0F);
		Core = new ModelRenderer(this, 0, 14);
		Core.addBox(-3F, 15F, -3F, 6, 6, 6);
		Core.setRotationPoint(0F, 0F, 0F);
		Core.setTextureSize(64, 32);
		Core.mirror = true;
		setRotation(Core, 0F, 0F, 0F);
		Movingthingright = new ModelRenderer(this, 46, 23);
		Movingthingright.addBox(-3F, -1F, -3F, 3, 3, 6);
		Movingthingright.setRotationPoint(-3F, 20F, 0F);
		Movingthingright.setTextureSize(64, 32);
		Movingthingright.mirror = true;
		setRotation(Movingthingright, 0F, 0F, 0F);
		Movingthingleft = new ModelRenderer(this, 46, 23);
		Movingthingleft.addBox(0F, -1F, -3F, 3, 3, 6);
		Movingthingleft.setRotationPoint(3F, 20F, 0F);
		Movingthingleft.setTextureSize(64, 32);
		Movingthingleft.mirror = true;
		setRotation(Movingthingleft, 0F, 0F, 0F);
		bottom = new ModelRenderer(this, 0, 0);
		bottom.addBox(-6F, 22F, -6F, 12, 2, 12);
		bottom.setRotationPoint(0F, 0F, 0F);
		bottom.setTextureSize(64, 32);
		bottom.mirror = true;
		setRotation(bottom, 0F, 0F, 0F);
	}

	public void render(float movement, float f5)
	{
		corner1.render(f5);
		corner2.render(f5);
		corner3.render(f5);
		corner4.render(f5);
		Bout.render(f5);
		Baout.render(f5);
		Fout.render(f5);

		GL11.glPushMatrix();
		GL11.glRotatef(movement, 0, 1, 0);
		Tout.render(f5);
		GL11.glPopMatrix();

		Core.render(f5);
		Movingthingright.render(f5);
		Movingthingleft.render(f5);
		bottom.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}

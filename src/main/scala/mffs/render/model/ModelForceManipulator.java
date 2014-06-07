package mffs.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelForceManipulator extends ModelBase
{
	// fields
	ModelRenderer ElectrodePillar;
	ModelRenderer ElectrodeBase;
	ModelRenderer ElectrodeNode;
	ModelRenderer WallBottom;
	ModelRenderer WallFront;
	ModelRenderer WallBack;
	ModelRenderer WallLeft;
	ModelRenderer WallRight;
	ModelRenderer WallTop;

	public ModelForceManipulator()
	{
		textureWidth = 128;
		textureHeight = 128;

		ElectrodePillar = new ModelRenderer(this, 0, 32);
		ElectrodePillar.addBox(0F, 0F, 0F, 3, 3, 3);
		ElectrodePillar.setRotationPoint(-1.5F, 19F, -1.5F);
		ElectrodePillar.setTextureSize(128, 128);
		ElectrodePillar.mirror = true;
		setRotation(ElectrodePillar, 0F, 0F, 0F);
		ElectrodeBase = new ModelRenderer(this, 0, 39);
		ElectrodeBase.addBox(0F, 0F, 0F, 7, 2, 7);
		ElectrodeBase.setRotationPoint(-3.5F, 21.5F, -3.5F);
		ElectrodeBase.setTextureSize(128, 128);
		ElectrodeBase.mirror = true;
		setRotation(ElectrodeBase, 0F, 0F, 0F);
		ElectrodeNode = new ModelRenderer(this, 0, 49);
		ElectrodeNode.addBox(0F, 0F, 0F, 5, 5, 5);
		ElectrodeNode.setRotationPoint(-2.5F, 15F, -2.5F);
		ElectrodeNode.setTextureSize(128, 128);
		ElectrodeNode.mirror = true;
		setRotation(ElectrodeNode, 0F, 0F, 0F);
		WallBottom = new ModelRenderer(this, 0, 0);
		WallBottom.addBox(0F, 0F, 0F, 16, 1, 16);
		WallBottom.setRotationPoint(-8F, 23F, -8F);
		WallBottom.setTextureSize(128, 128);
		WallBottom.mirror = true;
		setRotation(WallBottom, 0F, 0F, 0F);
		WallFront = new ModelRenderer(this, 65, 0);
		WallFront.addBox(0F, 0F, 0F, 16, 15, 1);
		WallFront.setRotationPoint(-8F, 8F, -8F);
		WallFront.setTextureSize(128, 128);
		WallFront.mirror = true;
		setRotation(WallFront, 0F, 0F, 0F);
		WallBack = new ModelRenderer(this, 65, 17);
		WallBack.addBox(0F, 0F, 0F, 16, 15, 1);
		WallBack.setRotationPoint(-8F, 8F, 7F);
		WallBack.setTextureSize(128, 128);
		WallBack.mirror = true;
		setRotation(WallBack, 0F, 0F, 0F);
		WallLeft = new ModelRenderer(this, 30, 50);
		WallLeft.addBox(0F, 0F, 0F, 1, 15, 14);
		WallLeft.setRotationPoint(-8F, 8F, -7F);
		WallLeft.setTextureSize(128, 128);
		WallLeft.mirror = true;
		setRotation(WallLeft, 0F, 0F, 0F);
		WallRight = new ModelRenderer(this, 30, 19);
		WallRight.addBox(0F, 0F, 0F, 1, 15, 14);
		WallRight.setRotationPoint(7F, 8F, -7F);
		WallRight.setTextureSize(128, 128);
		WallRight.mirror = true;
		setRotation(WallRight, 0F, 0F, 0F);
		WallTop = new ModelRenderer(this, 61, 36);
		WallTop.addBox(0F, 0F, 0F, 14, 1, 14);
		WallTop.setRotationPoint(-7F, 8F, -7F);
		WallTop.setTextureSize(128, 128);
		WallTop.mirror = true;
		setRotation(WallTop, 0F, 0F, 0F);
	}

	public void render(float f5)
	{
		ElectrodePillar.render(f5);
		ElectrodeBase.render(f5);
		ElectrodeNode.render(f5);
		WallBottom.render(f5);
		WallFront.render(f5);
		WallBack.render(f5);
		WallLeft.render(f5);
		WallRight.render(f5);
		WallTop.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}

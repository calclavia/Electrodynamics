package resonantinduction.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelFrackingPipe extends ModelBase
{
	ModelRenderer Back;
	ModelRenderer Top;
	ModelRenderer Left;
	ModelRenderer Right;
	ModelRenderer Front;
	ModelRenderer Bottom;
	ModelRenderer Pipe;

	public ModelFrackingPipe()
	{
		textureWidth = 128;
		textureHeight = 64;

		Back = new ModelRenderer(this, 35, 18);
		Back.addBox(0F, 0F, 0F, 14, 14, 1);
		Back.setRotationPoint(-7F, 9F, 7F);
		Back.setTextureSize(128, 64);
		Back.mirror = true;
		setRotation(Back, 0F, 0F, 0F);
		Top = new ModelRenderer(this, 0, 0);
		Top.addBox(0F, 0F, 0F, 16, 1, 16);
		Top.setRotationPoint(-8F, 8F, -8F);
		Top.setTextureSize(128, 64);
		Top.mirror = true;
		setRotation(Top, 0F, 0F, 0F);
		Left = new ModelRenderer(this, 0, 18);
		Left.addBox(0F, 0F, 0F, 1, 14, 16);
		Left.setRotationPoint(-8F, 9F, -8F);
		Left.setTextureSize(128, 64);
		Left.mirror = true;
		setRotation(Left, 0F, 0F, 0F);
		Right = new ModelRenderer(this, 0, 18);
		Right.addBox(0F, 0F, 0F, 1, 14, 16);
		Right.setRotationPoint(7F, 9F, -8F);
		Right.setTextureSize(128, 64);
		Right.mirror = true;
		setRotation(Right, 0F, 0F, 0F);
		Front = new ModelRenderer(this, 35, 18);
		Front.addBox(0F, 0F, 0F, 14, 14, 1);
		Front.setRotationPoint(-7F, 9F, -8F);
		Front.setTextureSize(128, 64);
		Front.mirror = true;
		setRotation(Front, 0F, 0F, 0F);
		Bottom = new ModelRenderer(this, 0, 0);
		Bottom.addBox(0F, 0F, 0F, 16, 1, 16);
		Bottom.setRotationPoint(-8F, 23F, -8F);
		Bottom.setTextureSize(128, 64);
		Bottom.mirror = true;
		setRotation(Bottom, 0F, 0F, 0F);
		Pipe = new ModelRenderer(this, 35, 34);
		Pipe.addBox(0F, 0F, 0F, 6, 14, 6);
		Pipe.setRotationPoint(-3F, 9F, -3F);
		Pipe.setTextureSize(128, 64);
		Pipe.mirror = true;
		setRotation(Pipe, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}

	public void renderAll()
	{
		Back.render(0.0625F);
		Top.render(0.0625F);
		Left.render(0.0625F);
		Right.render(0.0625F);
		Front.render(0.0625F);
		Bottom.render(0.0625F);
		Pipe.render(0.0625F);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
	{
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}

}

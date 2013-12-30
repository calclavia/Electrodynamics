package resonantinduction.transformer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelTransformer extends ModelBase
{
	// fields
	ModelRenderer a;
	ModelRenderer b;
	ModelRenderer c;
	ModelRenderer d;
	ModelRenderer out2;
	ModelRenderer out1;
	ModelRenderer out3;
	ModelRenderer out4;
	ModelRenderer i;
	ModelRenderer j;
	ModelRenderer in1;
	ModelRenderer in2;
	ModelRenderer in3;
	ModelRenderer in4;

	public ModelTransformer()
	{
		textureWidth = 70;
		textureHeight = 45;

		a = new ModelRenderer(this, 0, 0);
		a.addBox(-8F, 0F, -8F, 16, 2, 16);
		a.setRotationPoint(0F, 22F, 0F);
		a.setTextureSize(70, 45);
		a.mirror = true;
		setRotation(a, 0F, 0F, 0F);
		b = new ModelRenderer(this, 0, 19);
		b.addBox(0F, 0F, -2F, 3, 11, 4);
		b.setRotationPoint(5F, 11F, 0F);
		b.setTextureSize(70, 45);
		b.mirror = true;
		setRotation(b, 0F, 0F, 0F);
		c = new ModelRenderer(this, 0, 19);
		c.addBox(0F, 0F, -2F, 3, 11, 4);
		c.setRotationPoint(-8F, 11F, 0F);
		c.setTextureSize(70, 45);
		c.mirror = true;
		setRotation(c, 0F, 0F, 0F);
		d = new ModelRenderer(this, 15, 19);
		d.addBox(0F, 0F, -2F, 16, 1, 4);
		d.setRotationPoint(-8F, 10F, 0F);
		d.setTextureSize(70, 45);
		d.mirror = true;
		setRotation(d, 0F, 0F, 0F);
		out2 = new ModelRenderer(this, 0, 35);
		out2.addBox(0F, 0F, -3F, 5, 0, 6);
		out2.setRotationPoint(-9F, 16F, 0F);
		out2.setTextureSize(70, 45);
		out2.mirror = true;
		setRotation(out2, 0F, 0F, 0F);
		out1 = new ModelRenderer(this, 0, 35);
		out1.addBox(0F, 0F, -3F, 5, 0, 6);
		out1.setRotationPoint(-9F, 15F, 0F);
		out1.setTextureSize(70, 45);
		out1.mirror = true;
		setRotation(out1, 0F, 0F, 0F);
		out3 = new ModelRenderer(this, 0, 35);
		out3.addBox(0F, 0F, -3F, 5, 0, 6);
		out3.setRotationPoint(-9F, 17F, 0F);
		out3.setTextureSize(70, 45);
		out3.mirror = true;
		setRotation(out3, 0F, 0F, 0F);
		out4 = new ModelRenderer(this, 0, 35);
		out4.addBox(0F, 0F, -3F, 5, 0, 6);
		out4.setRotationPoint(-9F, 18F, 0F);
		out4.setTextureSize(70, 45);
		out4.mirror = true;
		setRotation(out4, 0F, 0F, 0F);
		i = new ModelRenderer(this, 34, 35);
		i.addBox(0F, 0F, -1F, 2, 5, 2);
		i.setRotationPoint(-10F, 14F, 0F);
		i.setTextureSize(70, 45);
		i.mirror = true;
		setRotation(i, 0F, 0F, 0F);
		j = new ModelRenderer(this, 24, 35);
		j.addBox(0F, 0F, -1F, 2, 5, 2);
		j.setRotationPoint(8F, 14F, 0F);
		j.setTextureSize(70, 45);
		j.mirror = true;
		setRotation(j, 0F, 0F, 0F);
		in1 = new ModelRenderer(this, 0, 35);
		in1.addBox(0F, 0F, -3F, 5, 0, 6);
		in1.setRotationPoint(4F, 15F, 0F);
		in1.setTextureSize(70, 45);
		in1.mirror = true;
		setRotation(in1, 0F, 0F, 0F);
		in2 = new ModelRenderer(this, 0, 35);
		in2.addBox(0F, 0F, -3F, 5, 0, 6);
		in2.setRotationPoint(4F, 16F, 0F);
		in2.setTextureSize(70, 45);
		in2.mirror = true;
		setRotation(in2, 0F, 0F, 0F);
		in3 = new ModelRenderer(this, 0, 35);
		in3.addBox(0F, 0F, -3F, 5, 0, 6);
		in3.setRotationPoint(4F, 17F, 0F);
		in3.setTextureSize(70, 45);
		in3.mirror = true;
		setRotation(in3, 0F, 0F, 0F);
		in4 = new ModelRenderer(this, 0, 35);
		in4.addBox(0F, 0F, -3F, 5, 0, 6);
		in4.setRotationPoint(4F, 18F, 0F);
		in4.setTextureSize(70, 45);
		in4.mirror = true;
		setRotation(in4, 0F, 0F, 0F);
	}

	public void render(float f5)
	{
		a.render(f5);
		b.render(f5);
		c.render(f5);
		d.render(f5);
		out2.render(f5);
		out1.render(f5);
		out3.render(f5);
		out4.render(f5);
		i.render(f5);
		j.render(f5);
		in1.render(f5);
		in2.render(f5);
		in3.render(f5);
		in4.render(f5);
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
package resonantinduction.mechanical.fluid.pipe;

import org.lwjgl.opengl.GL11;

import calclavia.lib.render.RenderUtility;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPipe extends ModelBase
{
	// fields
	ModelRenderer Body;
	ModelRenderer Deco1;
	ModelRenderer Deco4;
	ModelRenderer Deco2;
	ModelRenderer Deco3;
	ModelRenderer Deco5;
	ModelRenderer Deco6;
	ModelRenderer Deco7;
	ModelRenderer Deco8;
	ModelRenderer Deco9;
	ModelRenderer Body2;
	ModelRenderer Deco11;
	ModelRenderer Deco12;
	ModelRenderer Deco13;
	ModelRenderer Deco14;
	ModelRenderer Deco15;
	ModelRenderer Deco16;

	public ModelPipe()
	{
		textureWidth = 64;
		textureHeight = 32;

		Body = new ModelRenderer(this, 0, 0);
		Body.addBox(0F, 0F, 0F, 5, 5, 16);
		Body.setRotationPoint(-2F, 17F, -8F);
		Body.setTextureSize(64, 32);
		Body.mirror = true;
		setRotation(Body, 0F, 0F, 0F);
		Deco1 = new ModelRenderer(this, 42, 0);
		Deco1.addBox(0F, 0F, 0F, 5, 1, 1);
		Deco1.setRotationPoint(-2F, 16.5F, 0F);
		Deco1.setTextureSize(64, 32);
		Deco1.mirror = true;
		setRotation(Deco1, 0F, 0F, 0F);
		Deco4 = new ModelRenderer(this, 42, 0);
		Deco4.addBox(0F, 0F, 0F, 5, 1, 1);
		Deco4.setRotationPoint(-2F, 21.5F, 4F);
		Deco4.setTextureSize(64, 32);
		Deco4.mirror = true;
		setRotation(Deco4, 0F, 0F, 0F);
		Deco2 = new ModelRenderer(this, 42, 2);
		Deco2.addBox(0F, 0F, 0F, 6, 1, 1);
		Deco2.setRotationPoint(-1.5F, 16.5F, 4F);
		Deco2.setTextureSize(64, 32);
		Deco2.mirror = true;
		setRotation(Deco2, 0F, 0F, 1.570796F);
		Deco3 = new ModelRenderer(this, 42, 2);
		Deco3.addBox(0F, 0F, 0F, 6, 1, 1);
		Deco3.setRotationPoint(3.5F, 16.5F, 4F);
		Deco3.setTextureSize(64, 32);
		Deco3.mirror = true;
		setRotation(Deco3, 0F, 0F, 1.570796F);
		Deco5 = new ModelRenderer(this, 42, 0);
		Deco5.addBox(0F, 0F, 0F, 5, 1, 1);
		Deco5.setRotationPoint(-2F, 16.5F, 4F);
		Deco5.setTextureSize(64, 32);
		Deco5.mirror = true;
		setRotation(Deco5, 0F, 0F, 0F);
		Deco6 = new ModelRenderer(this, 42, 0);
		Deco6.addBox(0F, 0F, 0F, 5, 1, 1);
		Deco6.setRotationPoint(-2F, 21.5F, 0F);
		Deco6.setTextureSize(64, 32);
		Deco6.mirror = true;
		setRotation(Deco6, 0F, 0F, 0F);
		Deco7 = new ModelRenderer(this, 42, 2);
		Deco7.addBox(0F, 0F, -5F, 6, 1, 1);
		Deco7.setRotationPoint(-1.5F, 16.5F, 5F);
		Deco7.setTextureSize(64, 32);
		Deco7.mirror = true;
		setRotation(Deco7, 0F, 0F, 1.570796F);
		Deco8 = new ModelRenderer(this, 42, 2);
		Deco8.addBox(0F, 0F, 0F, 6, 1, 1);
		Deco8.setRotationPoint(3.5F, 16.5F, 0F);
		Deco8.setTextureSize(64, 32);
		Deco8.mirror = true;
		setRotation(Deco8, 0F, 0F, 1.570796F);
		Deco9 = new ModelRenderer(this, 42, 0);
		Deco9.addBox(0F, 0F, 0F, 5, 1, 1);
		Deco9.setRotationPoint(-2F, 16.5F, -4F);
		Deco9.setTextureSize(64, 32);
		Deco9.mirror = true;
		setRotation(Deco9, 0F, 0F, 0F);
		Body2 = new ModelRenderer(this, 42, 0);
		Body2.addBox(0F, 0F, 0F, 5, 1, 1);
		Body2.setRotationPoint(-2F, 21.5F, -4F);
		Body2.setTextureSize(64, 32);
		Body2.mirror = true;
		setRotation(Body2, 0F, 0F, 0F);
		Deco11 = new ModelRenderer(this, 42, 2);
		Deco11.addBox(0F, 0F, -5F, 6, 1, 1);
		Deco11.setRotationPoint(-1.5F, 16.5F, 1F);
		Deco11.setTextureSize(64, 32);
		Deco11.mirror = true;
		setRotation(Deco11, 0F, 0F, 1.570796F);
		Deco12 = new ModelRenderer(this, 42, 2);
		Deco12.addBox(0F, 0F, 0F, 6, 1, 1);
		Deco12.setRotationPoint(3.5F, 16.5F, -4F);
		Deco12.setTextureSize(64, 32);
		Deco12.mirror = true;
		setRotation(Deco12, 0F, 0F, 1.570796F);
		Deco13 = new ModelRenderer(this, 42, 0);
		Deco13.addBox(0F, 0F, 0F, 5, 1, 1);
		Deco13.setRotationPoint(-2F, 16.5F, -8F);
		Deco13.setTextureSize(64, 32);
		Deco13.mirror = true;
		setRotation(Deco13, 0F, 0F, 0F);
		Deco14 = new ModelRenderer(this, 42, 0);
		Deco14.addBox(0F, 0F, 0F, 5, 1, 1);
		Deco14.setRotationPoint(-2F, 21.5F, -8F);
		Deco14.setTextureSize(64, 32);
		Deco14.mirror = true;
		setRotation(Deco14, 0F, 0F, 0F);
		Deco15 = new ModelRenderer(this, 42, 2);
		Deco15.addBox(0F, 0F, -5F, 6, 1, 1);
		Deco15.setRotationPoint(-1.5F, 16.5F, -3F);
		Deco15.setTextureSize(64, 32);
		Deco15.mirror = true;
		setRotation(Deco15, 0F, 0F, 1.570796F);
		Deco16 = new ModelRenderer(this, 42, 2);
		Deco16.addBox(0F, 0F, -4F, 6, 1, 1);
		Deco16.setRotationPoint(3.5F, 16.5F, -4F);
		Deco16.setTextureSize(64, 32);
		Deco16.mirror = true;
		setRotation(Deco16, 0F, 0F, 1.570796F);
	}

	public void render(float f5)
	{
		Body.render(f5);
		Deco1.render(f5);
		Deco4.render(f5);
		Deco2.render(f5);
		Deco3.render(f5);
		Deco5.render(f5);
		Deco6.render(f5);
		Deco7.render(f5);
		Deco8.render(f5);
		Deco9.render(f5);
		Body2.render(f5);
		Deco11.render(f5);
		Deco12.render(f5);
		Deco13.render(f5);
		Deco14.render(f5);
		Deco15.render(f5);
		Deco16.render(f5);
	}

	public void render(byte side)
	{
		float f5 = 0.0625f;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			if (RenderUtility.canRenderSide(side, dir))
			{
				GL11.glPushMatrix();
				RenderUtility.rotateBlockBasedOnDirection(dir);
				Deco1.render(f5);
				Deco4.render(f5);
				Deco2.render(f5);
				Deco3.render(f5);
				Deco5.render(f5);
				Deco6.render(f5);
				Deco7.render(f5);
				Deco8.render(f5);
				Deco9.render(f5);
				Deco11.render(f5);
				Deco12.render(f5);
				Deco13.render(f5);
				Deco14.render(f5);
				Deco15.render(f5);
				Deco16.render(f5);
				GL11.glPopMatrix();

			}
		}

		Body.render(f5);
		Body2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}

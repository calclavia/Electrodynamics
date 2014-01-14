package resonantinduction.mechanical.belt;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelConveyorBelt extends ModelBase
{
	// fields
	ModelRenderer bBELTLong;
	ModelRenderer FBELT;
	ModelRenderer BacBELT;
	ModelRenderer BBelt;
	ModelRenderer FRL;
	ModelRenderer MRL;
	ModelRenderer FLL;
	ModelRenderer BLL;
	ModelRenderer MRoller;
	ModelRenderer BRoller;
	ModelRenderer tBELT;
	ModelRenderer FRoller;
	ModelRenderer BRL;
	ModelRenderer BML;
	ModelRenderer tBELTLong;
	ModelRenderer tBELT15;
	ModelRenderer bBELT15;
	ModelRenderer c4;
	ModelRenderer c3;
	ModelRenderer c2;
	ModelRenderer c1;

	public ModelConveyorBelt()
	{
		textureWidth = 128;
		textureHeight = 128;

		bBELTLong = new ModelRenderer(this, 0, 66);
		bBELTLong.addBox(0F, 0F, 0F, 14, 1, 16);
		bBELTLong.setRotationPoint(-7F, 22F, -8F);
		bBELTLong.setTextureSize(128, 128);
		bBELTLong.mirror = true;
		setRotation(bBELTLong, 0F, 0F, 0F);
		FBELT = new ModelRenderer(this, 0, 16);
		FBELT.addBox(0F, 0F, 0F, 14, 2, 1);
		FBELT.setRotationPoint(-7F, 20F, -8F);
		FBELT.setTextureSize(128, 128);
		FBELT.mirror = true;
		setRotation(FBELT, 0F, 0F, 0F);
		BacBELT = new ModelRenderer(this, 0, 16);
		BacBELT.addBox(0F, 0F, 0F, 14, 2, 1);
		BacBELT.setRotationPoint(-7F, 20F, 7F);
		BacBELT.setTextureSize(128, 128);
		BacBELT.mirror = true;
		setRotation(BacBELT, 0F, 0F, 0F);
		BBelt = new ModelRenderer(this, 0, 31);
		BBelt.addBox(0F, 0F, 0F, 14, 1, 14);
		BBelt.setRotationPoint(-7F, 22F, -7F);
		BBelt.setTextureSize(128, 128);
		BBelt.mirror = true;
		setRotation(BBelt, 0F, 0F, 0F);
		FRL = new ModelRenderer(this, 0, 20);
		FRL.addBox(0F, 0F, 0F, 1, 3, 2);
		FRL.setRotationPoint(-8F, 21F, -6F);
		FRL.setTextureSize(128, 128);
		FRL.mirror = true;
		setRotation(FRL, 0F, 0F, 0F);
		MRL = new ModelRenderer(this, 0, 20);
		MRL.addBox(0F, 0F, 0F, 1, 3, 2);
		MRL.setRotationPoint(-8F, 21F, -1F);
		MRL.setTextureSize(128, 128);
		MRL.mirror = true;
		setRotation(MRL, 0F, 0F, 0F);
		FLL = new ModelRenderer(this, 0, 20);
		FLL.addBox(0F, 0F, 0F, 1, 3, 2);
		FLL.setRotationPoint(7F, 21F, -6F);
		FLL.setTextureSize(128, 128);
		FLL.mirror = true;
		setRotation(FLL, 0F, 0F, 0F);
		BLL = new ModelRenderer(this, 0, 20);
		BLL.addBox(0F, 0F, 0F, 1, 3, 2);
		BLL.setRotationPoint(7F, 21F, 4F);
		BLL.setTextureSize(128, 128);
		BLL.mirror = true;
		setRotation(BLL, 0F, 0F, 0F);
		MRoller = new ModelRenderer(this, 0, 26);
		MRoller.addBox(-7F, -1F, -1F, 14, 2, 2);
		MRoller.setRotationPoint(0F, 21F, 0F);
		MRoller.setTextureSize(128, 128);
		MRoller.mirror = true;
		setRotation(MRoller, 0F, 0F, 0F);
		BRoller = new ModelRenderer(this, 0, 26);
		BRoller.addBox(-7F, -1F, -1F, 14, 2, 2);
		BRoller.setRotationPoint(0F, 21F, 5F);
		BRoller.setTextureSize(128, 128);
		BRoller.mirror = true;
		setRotation(BRoller, 0F, 0F, 0F);
		tBELT = new ModelRenderer(this, 0, 0);
		tBELT.addBox(0F, 0F, 0F, 14, 1, 14);
		tBELT.setRotationPoint(-7F, 19F, -7F);
		tBELT.setTextureSize(128, 128);
		tBELT.mirror = true;
		setRotation(tBELT, 0F, 0F, 0F);
		FRoller = new ModelRenderer(this, 0, 26);
		FRoller.addBox(-7F, -1F, -1F, 14, 2, 2);
		FRoller.setRotationPoint(0F, 21F, -5F);
		FRoller.setTextureSize(128, 128);
		FRoller.mirror = true;
		setRotation(FRoller, 0F, 0F, 0F);
		BRL = new ModelRenderer(this, 0, 20);
		BRL.addBox(0F, 0F, 0F, 1, 3, 2);
		BRL.setRotationPoint(-8F, 21F, 4F);
		BRL.setTextureSize(128, 128);
		BRL.mirror = true;
		setRotation(BRL, 0F, 0F, 0F);
		BML = new ModelRenderer(this, 0, 20);
		BML.addBox(0F, 0F, 0F, 1, 3, 2);
		BML.setRotationPoint(7F, 21F, -1F);
		BML.setTextureSize(128, 128);
		BML.mirror = true;
		setRotation(BML, 0F, 0F, 0F);
		tBELTLong = new ModelRenderer(this, 0, 48);
		tBELTLong.addBox(0F, 0F, 0F, 14, 1, 16);
		tBELTLong.setRotationPoint(-7F, 19F, -8F);
		tBELTLong.setTextureSize(128, 128);
		tBELTLong.mirror = true;
		setRotation(tBELTLong, 0F, 0F, 0F);
		// 15p long belts for end caps
		tBELT15 = new ModelRenderer(this, 0, 84);
		tBELT15.addBox(0F, 0F, 0F, 14, 1, 15);
		tBELT15.setRotationPoint(-7F, 19F, -8F);
		tBELT15.setTextureSize(128, 128);
		tBELT15.mirror = true;
		setRotation(tBELT15, 0F, 0F, 0F);
		bBELT15 = new ModelRenderer(this, 0, 84);
		bBELT15.addBox(0F, 0F, 0F, 14, 1, 15);
		bBELT15.setRotationPoint(-7F, 22F, -8F);
		bBELT15.setTextureSize(128, 128);
		bBELT15.mirror = true;
		setRotation(bBELT15, 0F, 0F, 0F);
		// bracers if connected to machane above
		c4 = new ModelRenderer(this, 60, 20);
		c4.addBox(0F, 0F, 0F, 1, 16, 1);
		c4.setRotationPoint(7F, 8F, 7F);
		c4.setTextureSize(128, 128);
		c4.mirror = true;
		setRotation(c4, 0F, 0F, 0F);
		c3 = new ModelRenderer(this, 60, 20);
		c3.addBox(0F, 0F, 0F, 1, 16, 1);
		c3.setRotationPoint(7F, 8F, -8F);
		c3.setTextureSize(128, 128);
		c3.mirror = true;
		setRotation(c3, 0F, 0F, 0F);
		c2 = new ModelRenderer(this, 60, 20);
		c2.addBox(0F, 0F, 0F, 1, 16, 1);
		c2.setRotationPoint(-8F, 8F, 7F);
		c2.setTextureSize(128, 128);
		c2.mirror = true;
		setRotation(c2, 0F, 0F, 0F);
		c1 = new ModelRenderer(this, 60, 20);
		c1.addBox(0F, 0F, 0F, 1, 16, 1);
		c1.setRotationPoint(-8F, 8F, -8F);
		c1.setTextureSize(128, 128);
		c1.mirror = true;
		setRotation(c1, 0F, 0F, 0F);
	}

	public void render(float f5, float radians, boolean front, boolean back, boolean above, boolean legs)
	{
		boolean mid = front && back ? true : false;
		boolean leftCap = !front && back ? true : false;
		boolean rightCap = front && !back ? true : false;
		if (back || front)
		{
			// use longer belts if needs to render
			// none normal

			if (leftCap)
			{
				FBELT.render(f5);
				tBELT15.setRotationPoint(-7F, 19F, -7F);
				bBELT15.setRotationPoint(-7F, 22F, -7F);
				tBELT15.render(f5);
				bBELT15.render(f5);
			}
			else if (rightCap)
			{
				BacBELT.render(f5);
				tBELT15.setRotationPoint(-7F, 19F, -8F);
				bBELT15.setRotationPoint(-7F, 22F, -8F);
				tBELT15.render(f5);
				bBELT15.render(f5);
			}
			else
			{
				bBELTLong.render(f5);
				tBELTLong.render(f5);
			}
		}
		else
		{
			// render normal if nothing is on
			// either side
			FBELT.render(f5);
			BacBELT.render(f5);
			BBelt.render(f5);
			tBELT.render(f5);
		}
		if (above)
		{
			c1.render(f5);
			c2.render(f5);
			c3.render(f5);
			c4.render(f5);
		}

		// rollers
		MRoller.rotateAngleX = radians;
		BRoller.rotateAngleX = radians;
		FRoller.rotateAngleX = radians;
		MRoller.render(f5);
		BRoller.render(f5);
		FRoller.render(f5);

		if (legs)
		{
			// legs
			BRL.render(f5);
			BML.render(f5);
			FLL.render(f5);
			BLL.render(f5);
			FRL.render(f5);
			MRL.render(f5);
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}

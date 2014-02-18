package resonantinduction.mechanical.logistic.belt;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelManipulator extends ModelBase
{
	// fields
	ModelRenderer bBELTLong;
	ModelRenderer FBELT;
	ModelRenderer BacPanel;
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
	ModelRenderer RPanel;
	ModelRenderer LPanel;
	ModelRenderer TopPanel;
	ModelRenderer RCPanel;
	ModelRenderer LCPanel;

	public ModelManipulator()
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
		BacPanel = new ModelRenderer(this, 0, 86);
		BacPanel.addBox(0F, -12F, 0F, 14, 12, 1);
		BacPanel.setRotationPoint(-7F, 24F, 7F);
		BacPanel.setTextureSize(128, 128);
		BacPanel.mirror = true;
		setRotation(BacPanel, 0F, 0F, 0F);
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
		RPanel = new ModelRenderer(this, 65, 41);
		RPanel.addBox(0F, -2F, -8F, 1, 4, 16);
		RPanel.setRotationPoint(-8F, 19F, 0F);
		RPanel.setTextureSize(128, 128);
		RPanel.mirror = true;
		setRotation(RPanel, 0F, 0F, 0F);
		LPanel = new ModelRenderer(this, 65, 20);
		LPanel.addBox(0F, -2F, -8F, 1, 4, 16);
		LPanel.setRotationPoint(7F, 19F, 0F);
		LPanel.setTextureSize(128, 128);
		LPanel.mirror = true;
		setRotation(LPanel, 0F, 0F, 0F);
		TopPanel = new ModelRenderer(this, 0, 105);
		TopPanel.addBox(0F, 0F, 0F, 14, 2, 10);
		TopPanel.setRotationPoint(-7F, 12F, -3F);
		TopPanel.setTextureSize(128, 128);
		TopPanel.mirror = true;
		setRotation(TopPanel, 0F, 0F, 0F);
		RCPanel = new ModelRenderer(this, 50, 105);
		RCPanel.addBox(-1F, 0F, 0F, 2, 5, 10);
		RCPanel.setRotationPoint(-7F, 14F, -3F);
		RCPanel.setTextureSize(128, 128);
		RCPanel.mirror = true;
		setRotation(RCPanel, 0F, 0F, 0F);
		LCPanel = new ModelRenderer(this, 76, 105);
		LCPanel.addBox(0F, 0F, 0F, 2, 5, 10);
		LCPanel.setRotationPoint(6F, 14F, -3F);
		LCPanel.setTextureSize(128, 128);
		LCPanel.mirror = true;
		setRotation(LCPanel, 0F, 0F, 0F);
	}

	public void render(float f5, boolean isLongBelt, int radians)
	{
		// body panels
		BacPanel.render(f5);
		RPanel.render(f5);
		LPanel.render(f5);
		TopPanel.render(f5);
		RCPanel.render(f5);
		LCPanel.render(f5);
		// legs
		FRL.render(f5);
		MRL.render(f5);
		FLL.render(f5);
		BLL.render(f5);
		BRL.render(f5);
		BML.render(f5);
		// rollers
		MRoller.rotateAngleX = radians;
		BRoller.rotateAngleX = radians;
		FRoller.rotateAngleX = radians;
		MRoller.render(f5);
		BRoller.render(f5);
		FRoller.render(f5);

		if (isLongBelt)
		{
			tBELTLong.render(f5);
			bBELTLong.render(f5);
		}
		else
		{
			FBELT.render(f5);
			tBELT.render(f5);
			BBelt.render(f5);
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}

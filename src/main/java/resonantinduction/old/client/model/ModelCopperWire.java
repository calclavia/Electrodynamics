package resonantinduction.old.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelCopperWire extends ModelBase
{
	// fields
	ModelRenderer Middle;
	ModelRenderer Right;
	ModelRenderer Left;
	ModelRenderer Back;
	ModelRenderer Front;
	ModelRenderer Top;
	ModelRenderer Bottom;

	public ModelCopperWire()
	{
		textureWidth = 64;
		textureHeight = 32;
		Middle = new ModelRenderer(this, 0, 0);
		Middle.addBox(-1F, -1F, -1F, 4, 4, 4);
		Middle.setRotationPoint(-1F, 15F, -1F);
		Middle.setTextureSize(64, 32);
		Middle.mirror = true;
		setRotation(Middle, 0F, 0F, 0F);
		Right = new ModelRenderer(this, 21, 0);
		Right.addBox(0F, 0F, 0F, 6, 4, 4);
		Right.setRotationPoint(2F, 14F, -2F);
		Right.setTextureSize(64, 32);
		Right.mirror = true;
		setRotation(Right, 0F, 0F, 0F);
		Left = new ModelRenderer(this, 21, 0);
		Left.addBox(0F, 0F, 0F, 6, 4, 4);
		Left.setRotationPoint(-8F, 14F, -2F);
		Left.setTextureSize(64, 32);
		Left.mirror = true;
		setRotation(Left, 0F, 0F, 0F);
		Back = new ModelRenderer(this, 0, 11);
		Back.addBox(0F, 0F, 0F, 4, 4, 6);
		Back.setRotationPoint(-2F, 14F, 2F);
		Back.setTextureSize(64, 32);
		Back.mirror = true;
		setRotation(Back, 0F, 0F, 0F);
		Front = new ModelRenderer(this, 0, 11);
		Front.addBox(0F, 0F, 0F, 4, 4, 6);
		Front.setRotationPoint(-2F, 14F, -8F);
		Front.setTextureSize(64, 32);
		Front.mirror = true;
		setRotation(Front, 0F, 0F, 0F);
		Top = new ModelRenderer(this, 21, 11);
		Top.addBox(0F, 0F, 0F, 4, 6, 4);
		Top.setRotationPoint(-2F, 8F, -2F);
		Top.setTextureSize(64, 32);
		Top.mirror = true;
		setRotation(Top, 0F, 0F, 0F);
		Bottom = new ModelRenderer(this, 21, 11);
		Bottom.addBox(0F, 0F, 0F, 4, 6, 4);
		Bottom.setRotationPoint(-2F, 18F, -2F);
		Bottom.setTextureSize(64, 32);
		Bottom.mirror = true;
		setRotation(Bottom, 0F, 0F, 0F);
	}

	public void renderSide(int i)
	{
		this.renderSide(ForgeDirection.getOrientation(i));
	}

	public void renderSide(ForgeDirection side)
	{
		switch (side)
		{
			case UP:
				Top.render(0.0625F);
				break;
			case DOWN:
				Bottom.render(0.0625F);
				break;
			case NORTH:
				Back.render(0.0625F);
				break;
			case SOUTH:
				Front.render(0.0625F);
				break;
			case WEST:
				Left.render(0.0625F);
				break;
			case EAST:
				Right.render(0.0625F);
				break;
			default:
				Middle.render(0.0625F);
				break;
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}

package resonantinduction.mechanical.fluid.pipe;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.common.ForgeDirection;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPipe extends ModelBase
{
	// fields
	ModelRenderer Mid;
	ModelRenderer RightPipe;
	ModelRenderer RightInter;
	ModelRenderer RightConnect;
	ModelRenderer LeftInter;
	ModelRenderer LeftPipe;
	ModelRenderer LeftConnect;
	ModelRenderer TopInter;
	ModelRenderer TopPipe;
	ModelRenderer TopConnect;
	ModelRenderer BottomPipe;
	ModelRenderer BottomInter;
	ModelRenderer BottomConnect;
	ModelRenderer BackPipe;
	ModelRenderer BackInter;
	ModelRenderer BackConnect;
	ModelRenderer FrontInter;
	ModelRenderer FrontPipe;
	ModelRenderer FrontConnect;

	public ModelPipe()
	{
		textureWidth = 128;
		textureHeight = 32;

		Mid = new ModelRenderer(this, 50, 13);
		Mid.addBox(-3F, -3F, -3F, 6, 6, 6);
		Mid.setRotationPoint(0F, 16F, 0F);
		Mid.setTextureSize(128, 32);
		Mid.mirror = true;
		setRotation(Mid, 0F, 0F, 0F);
		RightPipe = new ModelRenderer(this, 25, 0);
		RightPipe.addBox(0F, -3F, -3F, 4, 6, 6);
		RightPipe.setRotationPoint(3F, 16F, 0F);
		RightPipe.setTextureSize(128, 32);
		RightPipe.mirror = true;
		setRotation(RightPipe, 0F, 0F, 0F);
		RightInter = new ModelRenderer(this, 98, 0);
		RightInter.addBox(0F, -4F, -4F, 1, 8, 8);
		RightInter.setRotationPoint(2F, 16F, 0F);
		RightInter.setTextureSize(128, 32);
		RightInter.mirror = true;
		setRotation(RightInter, 0F, 0F, 0F);
		RightConnect = new ModelRenderer(this, 98, 0);
		RightConnect.addBox(0F, -4F, -4F, 1, 8, 8);
		RightConnect.setRotationPoint(7F, 16F, 0F);
		RightConnect.setTextureSize(128, 32);
		RightConnect.mirror = true;
		setRotation(RightConnect, 0F, 0F, 0F);
		LeftInter = new ModelRenderer(this, 98, 0);
		LeftInter.addBox(-1F, -4F, -4F, 1, 8, 8);
		LeftInter.setRotationPoint(-2F, 16F, 0F);
		LeftInter.setTextureSize(128, 32);
		LeftInter.mirror = true;
		setRotation(LeftInter, 0F, 0F, 0F);
		LeftPipe = new ModelRenderer(this, 25, 0);
		LeftPipe.addBox(-4F, -3F, -3F, 4, 6, 6);
		LeftPipe.setRotationPoint(-3F, 16F, 0F);
		LeftPipe.setTextureSize(128, 32);
		LeftPipe.mirror = true;
		setRotation(LeftPipe, 0F, 0F, 0F);
		LeftConnect = new ModelRenderer(this, 98, 0);
		LeftConnect.addBox(-1F, -4F, -4F, 1, 8, 8);
		LeftConnect.setRotationPoint(-7F, 16F, 0F);
		LeftConnect.setTextureSize(128, 32);
		LeftConnect.mirror = true;
		setRotation(LeftConnect, 0F, 0F, 0F);
		TopInter = new ModelRenderer(this, 77, 17);
		TopInter.addBox(-4F, -1F, -4F, 8, 1, 8);
		TopInter.setRotationPoint(0F, 14F, 0F);
		TopInter.setTextureSize(128, 32);
		TopInter.mirror = true;
		setRotation(TopInter, 0F, 0F, 0F);
		TopPipe = new ModelRenderer(this, 50, 0);
		TopPipe.addBox(-3F, -4F, -3F, 6, 4, 6);
		TopPipe.setRotationPoint(0F, 13F, 0F);
		TopPipe.setTextureSize(128, 32);
		TopPipe.mirror = true;
		setRotation(TopPipe, 0F, 0F, 0F);
		TopConnect = new ModelRenderer(this, 77, 17);
		TopConnect.addBox(-4F, -1F, -4F, 8, 1, 8);
		TopConnect.setRotationPoint(0F, 9F, 0F);
		TopConnect.setTextureSize(128, 32);
		TopConnect.mirror = true;
		setRotation(TopConnect, 0F, 0F, 0F);
		BottomPipe = new ModelRenderer(this, 50, 0);
		BottomPipe.addBox(-3F, 0F, -3F, 6, 4, 6);
		BottomPipe.setRotationPoint(0F, 19F, 0F);
		BottomPipe.setTextureSize(128, 32);
		BottomPipe.mirror = true;
		setRotation(BottomPipe, 0F, 0F, 0F);
		BottomInter = new ModelRenderer(this, 77, 17);
		BottomInter.addBox(-4F, 0F, -4F, 8, 1, 8);
		BottomInter.setRotationPoint(0F, 18F, 0F);
		BottomInter.setTextureSize(128, 32);
		BottomInter.mirror = true;
		setRotation(BottomInter, 0F, 0F, 0F);
		BottomConnect = new ModelRenderer(this, 77, 17);
		BottomConnect.addBox(-4F, 0F, -4F, 8, 1, 8);
		BottomConnect.setRotationPoint(0F, 23F, 0F);
		BottomConnect.setTextureSize(128, 32);
		BottomConnect.mirror = true;
		setRotation(BottomConnect, 0F, 0F, 0F);
		BackPipe = new ModelRenderer(this, 0, 0);
		BackPipe.addBox(-3F, -3F, 0F, 6, 6, 4);
		BackPipe.setRotationPoint(0F, 16F, 3F);
		BackPipe.setTextureSize(128, 32);
		BackPipe.mirror = true;
		setRotation(BackPipe, 0F, 0F, 0F);
		BackInter = new ModelRenderer(this, 0, 23);
		BackInter.addBox(-4F, -4F, 0F, 8, 8, 1);
		BackInter.setRotationPoint(0F, 16F, 2F);
		BackInter.setTextureSize(128, 32);
		BackInter.mirror = true;
		setRotation(BackInter, 0F, 0F, 0F);
		BackConnect = new ModelRenderer(this, 0, 23);
		BackConnect.addBox(-4F, -4F, 0F, 8, 8, 1);
		BackConnect.setRotationPoint(0F, 16F, 7F);
		BackConnect.setTextureSize(128, 32);
		BackConnect.mirror = true;
		setRotation(BackConnect, 0F, 0F, 0F);
		FrontInter = new ModelRenderer(this, 0, 23);
		FrontInter.addBox(-4F, -4F, -1F, 8, 8, 1);
		FrontInter.setRotationPoint(0F, 16F, -2F);
		FrontInter.setTextureSize(128, 32);
		FrontInter.mirror = true;
		setRotation(FrontInter, 0F, 0F, 0F);
		FrontPipe = new ModelRenderer(this, 0, 0);
		FrontPipe.addBox(-3F, -3F, -4F, 6, 6, 4);
		FrontPipe.setRotationPoint(0F, 16F, -3F);
		FrontPipe.setTextureSize(128, 32);
		FrontPipe.mirror = true;
		setRotation(FrontPipe, 0F, 0F, 0F);
		FrontConnect = new ModelRenderer(this, 0, 23);
		FrontConnect.addBox(-4F, -4F, -1F, 8, 8, 1);
		FrontConnect.setRotationPoint(0F, 16F, -7F);
		FrontConnect.setTextureSize(128, 32);
		FrontConnect.mirror = true;
		setRotation(FrontConnect, 0F, 0F, 0F);
	}

	public void render(byte side)
	{
		if (RenderUtility.canRenderSide(side, ForgeDirection.DOWN))
			renderBottom();
		if (RenderUtility.canRenderSide(side, ForgeDirection.UP))
			renderUp();
		if (RenderUtility.canRenderSide(side, ForgeDirection.NORTH))
			renderSouth();
		if (RenderUtility.canRenderSide(side, ForgeDirection.SOUTH))
			renderNorth();
		if (RenderUtility.canRenderSide(side, ForgeDirection.WEST))
			renderEast();
		if (RenderUtility.canRenderSide(side, ForgeDirection.EAST))
			renderWest();

		renderMiddle();
	}

	public void renderMiddle()
	{
		Mid.render(0.0625F);
	}

	public void renderBottom()
	{
		BottomPipe.render(0.0625F);
		BottomConnect.render(0.0625F);
		BottomInter.render(0.0625F);
	}

	public void renderUp()
	{
		TopPipe.render(0.0625F);
		TopConnect.render(0.0625F);
		TopInter.render(0.0625F);
	}

	public void renderEast()
	{
		LeftPipe.render(0.0625F);
		LeftConnect.render(0.0625F);
		LeftInter.render(0.0625F);
	}

	public void renderWest()
	{
		RightPipe.render(0.0625F);
		RightConnect.render(0.0625F);
		RightInter.render(0.0625F);
	}

	public void renderSouth()
	{
		BackPipe.render(0.0625F);
		BackConnect.render(0.0625F);
		BackInter.render(0.0625F);
	}

	public void renderNorth()
	{
		FrontPipe.render(0.0625F);
		FrontConnect.render(0.0625F);
		FrontInter.render(0.0625F);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}

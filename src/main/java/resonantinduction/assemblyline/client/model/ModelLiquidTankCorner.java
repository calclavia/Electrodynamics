package resonantinduction.assemblyline.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelLiquidTankCorner extends ModelBase
{
    // fields
    ModelRenderer sOne;
    ModelRenderer sTwo;
    ModelRenderer d7;
    ModelRenderer d5;
    ModelRenderer d3;
    ModelRenderer d4;
    ModelRenderer d1;
    ModelRenderer d6;
    ModelRenderer d2;
    ModelRenderer d8;
    ModelRenderer d9;
    ModelRenderer d10;
    ModelRenderer face;

    public ModelLiquidTankCorner()
    {
        textureWidth = 128;
        textureHeight = 128;

        sOne = new ModelRenderer(this, 0, 30);
        sOne.addBox(-1F, 0F, -1F, 2, 16, 2);
        sOne.setRotationPoint(-7F, 8F, 7F);
        sOne.setTextureSize(128, 128);
        sOne.mirror = true;
        setRotation(sOne, 0F, 0F, 0F);
        sTwo = new ModelRenderer(this, 0, 30);
        sTwo.addBox(-1F, 0F, -1F, 2, 16, 2);
        sTwo.setRotationPoint(-7F, 8F, -7F);
        sTwo.setTextureSize(128, 128);
        sTwo.mirror = true;
        setRotation(sTwo, 0F, 0F, 0F);
        d7 = new ModelRenderer(this, 43, 2);
        d7.addBox(-1F, 0F, -1F, 2, 16, 12);
        d7.setRotationPoint(-7F, 8F, -5F);
        d7.setTextureSize(128, 128);
        d7.mirror = true;
        setRotation(d7, 0F, 0F, 0F);
        d5 = new ModelRenderer(this, 9, 12);
        d5.addBox(-1F, 0F, -1F, 14, 16, 2);
        d5.setRotationPoint(-5F, 8F, 7F);
        d5.setTextureSize(128, 128);
        d5.mirror = true;
        setRotation(d5, 0F, 0F, 0F);
        d3 = new ModelRenderer(this, 9, 67);
        d3.addBox(-1.5F, 0F, -1.3F, 20, 2, 2);
        d3.setRotationPoint(-6F, 22F, -6F);
        d3.setTextureSize(128, 128);
        d3.mirror = true;
        setRotation(d3, 0F, -0.7853982F, 0F);
        d4 = new ModelRenderer(this, 9, 88);
        d4.addBox(0F, 0F, -9F, 5, 2, 4);
        d4.setRotationPoint(-6F, 22F, 6F);
        d4.setTextureSize(128, 128);
        d4.mirror = true;
        setRotation(d4, 0F, 0F, 0F);
        d1 = new ModelRenderer(this, 9, 67);
        d1.addBox(-1.5F, 0F, -1.3F, 20, 2, 2);
        d1.setRotationPoint(-6F, 8F, -6F);
        d1.setTextureSize(128, 128);
        d1.mirror = true;
        setRotation(d1, 0F, -0.7853982F, 0F);
        d6 = new ModelRenderer(this, 9, 75);
        d6.addBox(-1.5F, 0F, -1.3F, 17, 2, 2);
        d6.setRotationPoint(-6F, 22F, -4F);
        d6.setTextureSize(128, 128);
        d6.mirror = true;
        setRotation(d6, 0F, -0.7853982F, 0F);
        d2 = new ModelRenderer(this, 9, 80);
        d2.addBox(0F, 0F, -5F, 9, 2, 5);
        d2.setRotationPoint(-6F, 22F, 6F);
        d2.setTextureSize(128, 128);
        d2.mirror = true;
        setRotation(d2, 0F, 0F, 0F);
        d8 = new ModelRenderer(this, 9, 75);
        d8.addBox(-1.5F, 0F, -1.3F, 17, 2, 2);
        d8.setRotationPoint(-6F, 8F, -4F);
        d8.setTextureSize(128, 128);
        d8.mirror = true;
        setRotation(d8, 0F, -0.7853982F, 0F);
        d9 = new ModelRenderer(this, 9, 88);
        d9.addBox(0F, 0F, -9F, 5, 2, 4);
        d9.setRotationPoint(-6F, 8F, 6F);
        d9.setTextureSize(128, 128);
        d9.mirror = true;
        setRotation(d9, 0F, 0F, 0F);
        d10 = new ModelRenderer(this, 9, 80);
        d10.addBox(0F, 0F, -5F, 9, 2, 5);
        d10.setRotationPoint(-6F, 8F, 6F);
        d10.setTextureSize(128, 128);
        d10.mirror = true;
        setRotation(d10, 0F, 0F, 0F);
        face = new ModelRenderer(this, 0, 50);
        face.addBox(-8.5F, 0F, 0F, 17, 14, 2);
        face.setRotationPoint(0F, 9F, 0F);
        face.setTextureSize(128, 128);
        face.mirror = true;
        setRotation(face, 0F, -0.7853982F, 0F);
    }

    public void render(float f5)
    {
        sOne.render(f5);
        sTwo.render(f5);
        d7.render(f5);
        d5.render(f5);
        d3.render(f5);
        d4.render(f5);
        d1.render(f5);
        d6.render(f5);
        d2.render(f5);
        d8.render(f5);
        d9.render(f5);
        d10.render(f5);
        face.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}

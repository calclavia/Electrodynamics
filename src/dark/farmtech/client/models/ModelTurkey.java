// Date: 11/28/2013 6:18:31 AM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package dark.farmtech.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

public class ModelTurkey extends ModelBase
{
    //fields
    ModelRenderer head;
    ModelRenderer bill;
    ModelRenderer chin;
    ModelRenderer body;
    ModelRenderer body2;
    ModelRenderer rightLeg;
    ModelRenderer leftLeg;
    ModelRenderer rightWing;
    ModelRenderer leftWing;
    ModelRenderer neck;
    ModelRenderer fan;

    public ModelTurkey()
    {
        textureWidth = 128;
        textureHeight = 128;

        head = new ModelRenderer(this, 0, 0);
        head.addBox(0F, -1F, -4F, 4, 6, 3);
        head.setRotationPoint(-2F, 6F, -3F);
        head.setTextureSize(128, 128);
        head.mirror = true;
        setRotation(head, 0F, 0F, 0F);
        bill = new ModelRenderer(this, 14, 0);
        bill.addBox(0F, -1F, -4F, 4, 2, 2);
        bill.setRotationPoint(-2F, 8F, -5F);
        bill.setTextureSize(128, 128);
        bill.mirror = true;
        setRotation(bill, 0F, 0F, 0F);
        chin = new ModelRenderer(this, 14, 4);
        chin.addBox(0F, -1F, -4F, 2, 2, 2);
        chin.setRotationPoint(-1F, 10F, -4F);
        chin.setTextureSize(128, 128);
        chin.mirror = true;
        setRotation(chin, 0F, 0F, 0F);
        body = new ModelRenderer(this, 0, 9);
        body.addBox(0F, 0F, 0F, 8, 10, 9);
        body.setRotationPoint(-4F, 9F, -3F);
        body.setTextureSize(128, 128);
        body.mirror = true;
        setRotation(body, 0F, 0F, 0F);
        body2 = new ModelRenderer(this, 54, 9);
        body2.addBox(0F, 0F, 0F, 8, 8, 2);
        body2.setRotationPoint(-4F, 11F, -5F);
        body2.setTextureSize(128, 128);
        body2.mirror = true;
        setRotation(body2, 0.1396263F, 0F, 0F);
        rightLeg = new ModelRenderer(this, 0, 32);
        rightLeg.addBox(2F, 3F, 1F, 2, 5, 2);
        rightLeg.setRotationPoint(-1F, 16F, -2F);
        rightLeg.setTextureSize(128, 128);
        rightLeg.mirror = true;
        setRotation(rightLeg, 0F, 0F, 0F);
        leftLeg = new ModelRenderer(this, 0, 32);
        leftLeg.addBox(-2F, 3F, 1F, 2, 5, 2);
        leftLeg.setRotationPoint(-1F, 16F, -2F);
        leftLeg.setTextureSize(128, 128);
        leftLeg.mirror = true;
        setRotation(leftLeg, 0F, 0F, 0F);
        rightWing = new ModelRenderer(this, 36, 13);
        rightWing.addBox(-5F, -6F, 0F, 1, 7, 7);
        rightWing.setRotationPoint(0F, 16F, -2F);
        rightWing.setTextureSize(128, 128);
        rightWing.mirror = true;
        setRotation(rightWing, -0.1745329F, 0F, 0F);
        leftWing = new ModelRenderer(this, 36, 13);
        leftWing.addBox(5F, -6F, 0F, 1, 7, 7);
        leftWing.setRotationPoint(-1F, 16F, -2F);
        leftWing.setTextureSize(128, 128);
        leftWing.mirror = true;
        setRotation(leftWing, -0.1745329F, 0F, 0F);
        neck = new ModelRenderer(this, 34, 0);
        neck.addBox(0F, 0F, -4F, 3, 4, 4);
        neck.setRotationPoint(-1.5F, 8F, -2F);
        neck.setTextureSize(128, 128);
        neck.mirror = true;
        setRotation(neck, 0F, 0F, 0F);
        fan = new ModelRenderer(this, 0, 43);
        fan.addBox(0F, 0F, 0.05F, 12, 11, 0);
        fan.setRotationPoint(-6F, 4F, 6F);
        fan.setTextureSize(128, 128);
        fan.mirror = true;
        setRotation(fan, 0F, 0F, 0F);
    }

    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        this.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);

        if (this.isChild)
        {
            float f6 = 2.0F;
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 5.0F * par7, 2.0F * par7);
            this.head.render(par7);
            this.bill.render(par7);
            this.chin.render(par7);
            this.neck.render(par7);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
            GL11.glTranslatef(0.0F, 24.0F * par7, 0.0F);
            this.body.render(par7);
            this.body2.render(par7);
            this.fan.render(par7);
            this.rightLeg.render(par7);
            this.leftLeg.render(par7);
            this.rightWing.render(par7);
            this.leftWing.render(par7);
            GL11.glPopMatrix();
        }
        else
        {
            this.head.render(par7);
            this.bill.render(par7);
            this.chin.render(par7);
            this.body.render(par7);
            this.body2.render(par7);
            this.fan.render(par7);
            this.rightLeg.render(par7);
            this.leftLeg.render(par7);
            this.rightWing.render(par7);
            this.leftWing.render(par7);
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
    
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity)
    {
        this.head.rotateAngleX = par5 / (180F / (float)Math.PI);
        this.head.rotateAngleY = par4 / (180F / (float)Math.PI);
        this.bill.rotateAngleX = this.head.rotateAngleX;
        this.bill.rotateAngleY = this.head.rotateAngleY;
        this.chin.rotateAngleX = this.head.rotateAngleX;
        this.chin.rotateAngleY = this.head.rotateAngleY;
        //this.body.rotateAngleX = ((float)Math.PI / 2F);
        //this.body2.rotateAngleX = ((float)Math.PI / 2F);
        //this.fan.rotateAngleX = ((float)Math.PI / 2F);
        //this.neck.rotateAngleX = ((float)Math.PI / 2F);
        this.rightLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
        this.leftLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * 1.4F * par2;
        this.rightWing.rotateAngleZ = par3;
        this.leftWing.rotateAngleZ = -par3;
    }

}

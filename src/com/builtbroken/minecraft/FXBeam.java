package com.builtbroken.minecraft;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;


import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Based off Thaumcraft's Beam Renderer.
 *
 * @author Calclavia, Azanor */
@SideOnly(Side.CLIENT)
public class FXBeam extends EntityFX
{
    double movX = 0.0D, movY = 0.0D, movZ = 0.0D;

    private float length = 0.0F;

    private float rotYaw = 0.0F, rotPitch = 0.0F, rotSpeed = 20;

    private float prevYaw = 0.0F, prevPitch = 0.0F;

    private Vector3 endLocation = new Vector3();

    private float endModifier = 1.0F;

    /** Reverse rotation */
    private boolean reverse = false;
    /** pulse or fade as the life span goes down */
    private boolean pulse = false;

    private float prevSize = 0.0F;
    /** beam diameter */
    private float beamD = 0.08f;

    private String texture = DarkCore.TEXTURE_DIRECTORY + "";

    public FXBeam(World world, Vector3 start, Vector3 end, Color color, String texture, int age, boolean pulse)
    {
        this(world, start, end, color, texture, age);
        this.pulse = pulse;
    }

    public FXBeam(World world, Vector3 start, Vector3 end, Color color, String texture, int age)
    {
        super(world, start.x, start.y, start.z, 0.0D, 0.0D, 0.0D);

        this.setRGB(color);

        this.texture = texture;

        this.setSize(0.02F, 0.02F);
        this.noClip = true;

        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;

        this.endLocation = end;
        float xd = (float) (this.posX - this.endLocation.x);
        float yd = (float) (this.posY - this.endLocation.y);
        float zd = (float) (this.posZ - this.endLocation.z);
        this.length = (float) new Vector3(this).distance(this.endLocation);
        double var7 = MathHelper.sqrt_double(xd * xd + zd * zd);
        this.rotYaw = ((float) (Math.atan2(xd, zd) * 180.0D / 3.141592653589793D));
        this.rotPitch = ((float) (Math.atan2(yd, var7) * 180.0D / 3.141592653589793D));
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;

        this.particleMaxAge = age;

        /** Sets the particle age based on distance. */
        EntityLivingBase renderentity = Minecraft.getMinecraft().renderViewEntity;
        int visibleDistance = 50;

        if (!Minecraft.getMinecraft().gameSettings.fancyGraphics)
        {
            visibleDistance = 25;
        }
        if (renderentity.getDistance(this.posX, this.posY, this.posZ) > visibleDistance)
        {
            this.particleMaxAge = 0;
        }
    }

    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;

        float deltaX = (float) (this.posX - this.endLocation.x);
        float deltaY = (float) (this.posY - this.endLocation.y);
        float deltaZ = (float) (this.posZ - this.endLocation.z);

        this.length = MathHelper.sqrt_float(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        double xzMag = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);

        this.rotYaw = ((float) (Math.atan2(deltaX, deltaZ) * 180.0D / 3.141592653589793D));
        this.rotPitch = ((float) (Math.atan2(deltaY, xzMag) * 180.0D / 3.141592653589793D));

        if (this.particleAge++ >= this.particleMaxAge)
        {
            setDead();
        }
    }

    public void setRGB(float r, float g, float b)
    {
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
    }

    public void setRGB(Color color)
    {
        this.particleRed = color.getRed();
        this.particleGreen = color.getGreen();
        this.particleBlue = color.getBlue();
    }

    @Override
    public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5)
    {
        //Clear
        tessellator.draw();
        GL11.glPushMatrix();

        //Start drawing
        try
        {
            float var9 = 1.0F;
            float slide = this.worldObj.getTotalWorldTime();
            float rot = this.worldObj.provider.getWorldTime() % (360 / this.rotSpeed) * this.rotSpeed + this.rotSpeed * f;

            float size = 1.0F;
            float alphaColor = 0.5F;

            if (this.pulse)
            {
                size = Math.min(this.particleAge / 4.0F, 1.0F);
                size = this.prevSize + (size - this.prevSize) * f;
                if ((this.particleMaxAge - this.particleAge <= 4))
                {
                    alphaColor = 0.5F - (4 - (this.particleMaxAge - this.particleAge)) * 0.1F;
                }

            }

            FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(this.texture));

            GL11.glTexParameterf(3553, 10242, 10497.0F);
            GL11.glTexParameterf(3553, 10243, 10497.0F);

            GL11.glDisable(2884);

            float var11 = slide + f;
            if (this.reverse)
            {
                var11 *= -1.0F;
            }
            float var12 = -var11 * 0.2F - MathHelper.floor_float(-var11 * 0.1F);

            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 1);
            GL11.glDepthMask(false);

            float xx = (float) (this.prevPosX + (this.posX - this.prevPosX) * f - interpPosX);
            float yy = (float) (this.prevPosY + (this.posY - this.prevPosY) * f - interpPosY);
            float zz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * f - interpPosZ);
            GL11.glTranslated(xx, yy, zz);

            float yaw = this.prevYaw + (this.rotYaw - this.prevYaw) * f;
            float pitch = this.prevPitch + (this.rotPitch - this.prevPitch) * f;
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F + yaw, 0.0F, 0.0F, -1.0F);
            GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);

            double negWidth = -beamD * size;
            double posWidth = beamD * size;
            //TODO test length without size to see if that will fix length render changes
            double negLength = -beamD * size * this.endModifier;
            double posLength = beamD * size * this.endModifier;

            GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
            for (int t = 0; t < 3; t++)
            {
                double dist = this.length * size * var9;
                double var31 = 0.0D;
                double var33 = 1.0D;
                double var35 = -1.0F + var12 + t / 3.0F;
                double uvLength = this.length * size * var9 + var35;

                GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
                tessellator.startDrawingQuads();
                tessellator.setBrightness(200);
                tessellator.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, alphaColor);
                tessellator.addVertexWithUV(negLength, dist, 0.0D, var33, uvLength);
                tessellator.addVertexWithUV(negWidth, 0.0D, 0.0D, var33, var35);
                tessellator.addVertexWithUV(posWidth, 0.0D, 0.0D, var31, var35);
                tessellator.addVertexWithUV(posLength, dist, 0.0D, var31, uvLength);
                tessellator.draw();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
            GL11.glEnable(2884);

            this.prevSize = size;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //Reset
        GL11.glPopMatrix();
        tessellator.startDrawingQuads();
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation("textures/particle/particles.png"));
    }
}
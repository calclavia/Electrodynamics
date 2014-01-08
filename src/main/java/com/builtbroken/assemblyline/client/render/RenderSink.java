package com.builtbroken.assemblyline.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.client.model.ModelSink;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSink extends TileEntitySpecialRenderer
{
    int type = 0;
    private ModelSink model;

    public static final ResourceLocation TEXTURE = new ResourceLocation(AssemblyLine.DOMAIN, AssemblyLine.MODEL_DIRECTORY + "Sink.png");

    public RenderSink()
    {
        model = new ModelSink();
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double d, double d1, double d2, float f)
    {
        int meta = te.worldObj.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord);

        bindTexture(TEXTURE);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        switch (meta)
        {
            case 3:
                GL11.glRotatef(90f, 0f, 1f, 0f);
                break;
            case 0:
                GL11.glRotatef(180f, 0f, 1f, 0f);
                break;
            case 1:
                GL11.glRotatef(270f, 0f, 1f, 0f);
                break;
            case 2:
                GL11.glRotatef(0f, 0f, 1f, 0f);
                break;
        }
        model.render(0.0625F);
        //TODO render fluid using fluid renderer
        GL11.glPopMatrix();

    }

}
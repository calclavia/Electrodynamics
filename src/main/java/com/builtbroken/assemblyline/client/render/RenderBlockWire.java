package com.builtbroken.assemblyline.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.client.model.ModelCopperWire;
import com.builtbroken.assemblyline.transmit.TileEntityWire;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockWire extends TileEntitySpecialRenderer
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(AssemblyLine.DOMAIN, "textures/models/copperWire.png");

    public static final ModelCopperWire model = new ModelCopperWire();

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double d, double d1, double d2, float f)
    {
        // Texture file
        this.bindTexture(TEXTURE);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);

        if (tileEntity instanceof TileEntityWire)
        {
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
            {
                if (((TileEntityWire) tileEntity).hasConnectionSide(side))
                {
                    model.renderSide(side);
                }
            }
        }
        model.renderSide(ForgeDirection.UNKNOWN);
        GL11.glPopMatrix();
    }
}
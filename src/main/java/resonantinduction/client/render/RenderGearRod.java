package resonantinduction.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import resonantinduction.client.model.ModelGearRod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGearRod extends TileEntitySpecialRenderer
{
    private ModelGearRod model;

    public RenderGearRod()
    {
        model = new ModelGearRod();
    }

    public void renderAModelAt(TileEntity tileEntity, double d, double d1, double d2, float f)
    {
        //bindTextureByName(FluidMech.MODEL_TEXTURE_DIRECTORY + "mechanical/GearRod.png");
        GL11.glPushMatrix();

        int meta = tileEntity.worldObj.getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
        if (meta == 0)
        {
            GL11.glTranslatef((float) d + 0.5F, (float) d1 + 0.5F, (float) d2 + 1.5F);
        }
        else if (meta == 1)
        {
            GL11.glTranslatef((float) d + 0.5F, (float) d1 + 0.5F, (float) d2 - 0.5F);
        }
        else
        {
            GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        }
        GL11.glScalef(1.0F, -1F, -1F);
        switch (meta)
        {
            case 0:
                GL11.glRotatef(90f, 1f, 0f, 0f);
                break;
            case 1:
                GL11.glRotatef(-90f, 1f, 0f, 0f);
                break;
            case 2:
                GL11.glRotatef(0f, 0f, 1f, 0f);
                break;
            case 5:
                GL11.glRotatef(90f, 0f, 1f, 0f);
                break;
            case 3:
                GL11.glRotatef(180f, 0f, 1f, 0f);
                break;
            case 4:
                GL11.glRotatef(270f, 0f, 1f, 0f);
                break;
        }
        model.render(0.0625F, 0);
        GL11.glPopMatrix();

    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
    {
        this.renderAModelAt(tileEntity, var2, var4, var6, var8);
    }

}
package resonantinduction.assemblyline.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.assemblyline.AssemblyLine;
import resonantinduction.assemblyline.client.model.ModelGearPiston;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGearPiston extends TileEntitySpecialRenderer
{
    private ModelGearPiston model;
    public static final ResourceLocation TEXTURE = new ResourceLocation(AssemblyLine.DOMAIN, AssemblyLine.MODEL_DIRECTORY + "GearShaftPiston.png");

    public RenderGearPiston()
    {
        model = new ModelGearPiston();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double d, double d1, double d2, float f)
    {
        bindTexture(TEXTURE);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        int meta = tileEntity.worldObj.getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);

        switch (meta)
        {
            case 1:
                GL11.glRotatef(0f, 0f, 1f, 0f);
                break;
            case 2:
                GL11.glRotatef(90f, 0f, 1f, 0f);
                break;
            case 3:
                GL11.glRotatef(180f, 0f, 1f, 0f);
                break;
            case 0:
                GL11.glRotatef(270f, 0f, 1f, 0f);
                break;
        }
        model.renderGear(0.0625F);
        model.renderR(0.0625F, 0);//TODO fix
        model.renderBody(0.0625F);
        model.renderBack(0.0625F);
        model.renderFront(0.0625F);
        model.renderLeft(0.0625F);
        model.renderRight(0.0625F);
        GL11.glPopMatrix();
    }

}
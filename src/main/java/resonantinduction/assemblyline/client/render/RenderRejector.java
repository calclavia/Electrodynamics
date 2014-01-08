package resonantinduction.assemblyline.client.render;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.assemblyline.AssemblyLine;
import resonantinduction.assemblyline.client.model.ModelRejectorPiston;
import resonantinduction.transport.logistic.TileEntityRejector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRejector extends RenderImprintable
{
    private ModelRejectorPiston model = new ModelRejectorPiston();

    private void renderAModelAt(TileEntityRejector tileEntity, double x, double y, double z, float f)
    {
        boolean fire = tileEntity.firePiston;
        int face = tileEntity.getDirection().ordinal();
        int pos = 0;

        if (fire)
        {
            pos = 8;
        }
        ResourceLocation name = new ResourceLocation(AssemblyLine.DOMAIN, AssemblyLine.MODEL_DIRECTORY + "rejector.png");
        bindTexture(name);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        if (face == 2)
        {
            GL11.glRotatef(180f, 0f, 1f, 0f);
        }
        if (face == 3)
        {
            GL11.glRotatef(0f, 0f, 1f, 0f);
        }
        else if (face == 4)
        {
            GL11.glRotatef(90f, 0f, 1f, 0f);
        }
        else if (face == 5)
        {
            GL11.glRotatef(270f, 0f, 1f, 0f);
        }
        model.render(0.0625F);
        model.renderPiston(0.0625F, pos);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
    {
        this.renderAModelAt((TileEntityRejector) tileEntity, var2, var4, var6, var8);
        super.renderTileEntityAt(tileEntity, var2, var4, var6, var8);
    }

}
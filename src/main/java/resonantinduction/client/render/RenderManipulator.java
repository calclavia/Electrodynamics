package resonantinduction.client.render;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.AssemblyLine;
import resonantinduction.client.model.ModelManipulator;
import resonantinduction.transport.logistic.TileEntityManipulator;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderManipulator extends RenderImprintable
{
    private ModelManipulator model = new ModelManipulator();

    private void renderAModelAt(TileEntityManipulator tileEntity, double x, double y, double z, float f)
    {
        int face = tileEntity.getDirection().ordinal();

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glRotatef(180f, 0f, 0f, 1f);

        if (tileEntity.isOutput())
        {
            ResourceLocation name = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "manipulator1.png");
            bindTexture(name);
        }
        else
        {
            ResourceLocation name = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "manipulator2.png");
            bindTexture(name);
        }

        if (face == 2)
        {
            GL11.glRotatef(0f, 0f, 1f, 0f);
        }
        else if (face == 3)
        {
            GL11.glRotatef(180f, 0f, 1f, 0f);
        }
        else if (face == 4)
        {
            GL11.glRotatef(270f, 0f, 1f, 0f);
        }
        else if (face == 5)
        {
            GL11.glRotatef(90f, 0f, 1f, 0f);
        }

        model.render(0.0625F, true, 0);

        GL11.glPopMatrix();

    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
    {
        this.renderAModelAt((TileEntityManipulator) tileEntity, var2, var4, var6, var8);
        super.renderTileEntityAt(tileEntity, var2, var4, var6, var8);
    }

}
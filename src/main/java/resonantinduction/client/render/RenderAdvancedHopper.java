package resonantinduction.client.render;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.AssemblyLine;
import resonantinduction.client.model.ModelAdvancedHopper;
import resonantinduction.transport.hopper.TileEntityAdvancedHopper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAdvancedHopper extends RenderImprintable
{
    private static final ModelAdvancedHopper MODEL = new ModelAdvancedHopper();
    public static final ResourceLocation TEXTURE = new ResourceLocation(AssemblyLine.instance.DOMAIN, AssemblyLine.MODEL_DIRECTORY + "AdvancedHopper.png");

    private void renderAModelAt(TileEntityAdvancedHopper tileEntity, double x, double y, double z, float f)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glRotatef(180f, 0f, 0f, 1f);

        bindTexture(TEXTURE);

        MODEL.render(0.0625F);

        if (tileEntity.singleConnection)
        {
            MODEL.render(0.0625F, tileEntity.connection);
        }
        else
        {
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
            {
                if (tileEntity.connections[direction.ordinal()])
                {
                    MODEL.render(0.0625F, direction);
                }
            }
        }

        GL11.glPopMatrix();

    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
    {
        this.renderAModelAt((TileEntityAdvancedHopper) tileEntity, var2, var4, var6, var8);
    }

}
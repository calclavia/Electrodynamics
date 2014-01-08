package resonantinduction.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.AssemblyLine;
import resonantinduction.client.model.ModelSolarPanel;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockSolarPanel extends TileEntitySpecialRenderer
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "SolarPanel.png");

    public static final ModelSolarPanel model = new ModelSolarPanel();

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
    {
        // Texture file
        this.bindTexture(TEXTURE);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        model.render(0.0625F);
        GL11.glPopMatrix();
    }
}
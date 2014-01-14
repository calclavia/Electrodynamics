package resonantinduction.mechanical.fluid.valve;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.mechanical.fluid.pipe.ModelPipe;
import resonantinduction.mechanical.fluid.pipe.RenderPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderReleaseValve extends TileEntitySpecialRenderer
{
    public static ModelReleaseValve valve = new ModelReleaseValve();
    private TileEntity[] ents = new TileEntity[6];

    public static final ResourceLocation VALVE_TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "ReleaseValve.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double d, double d1, double d2, float f)
    {
        // Texture file
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        if (te instanceof TileReleaseValve)
        {
            ents = ((TileReleaseValve) te).connected;
        }
        bindTexture(RenderPipe.TEXTURE);
        if (ents[0] != null)
            RenderPipe.MODEL_PIPE.renderBottom();
        if (ents[1] != null)
            RenderPipe.MODEL_PIPE.renderTop();
        if (ents[3] != null)
            RenderPipe.MODEL_PIPE.renderFront();
        if (ents[2] != null)
            RenderPipe.MODEL_PIPE.renderBack();
        if (ents[5] != null)
            RenderPipe.MODEL_PIPE.renderRight();
        if (ents[4] != null)
            RenderPipe.MODEL_PIPE.renderLeft();
        RenderPipe.MODEL_PIPE.renderMiddle();
        bindTexture(VALVE_TEXTURE);
        if (ents[1] == null)
            valve.render();
        GL11.glPopMatrix();

    }
}
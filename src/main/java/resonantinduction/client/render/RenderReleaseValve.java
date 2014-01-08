package resonantinduction.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.AssemblyLine;
import resonantinduction.client.model.ModelLargePipe;
import resonantinduction.client.model.ModelReleaseValve;
import resonantinduction.transport.fluid.TileEntityReleaseValve;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderReleaseValve extends TileEntitySpecialRenderer
{
    private ModelLargePipe SixPipe;
    private ModelReleaseValve valve;
    private TileEntity[] ents = new TileEntity[6];

    public static final ResourceLocation VALVE_TEXTURE = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "ReleaseValve.png");

    public RenderReleaseValve()
    {
        SixPipe = new ModelLargePipe();
        valve = new ModelReleaseValve();
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double d, double d1, double d2, float f)
    {
        // Texture file
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        if (te instanceof TileEntityReleaseValve)
        {
            ents = ((TileEntityReleaseValve) te).connected;
        }
        bindTexture(RenderPipe.TEXTURE);
        if (ents[0] != null)
            SixPipe.renderBottom();
        if (ents[1] != null)
            SixPipe.renderTop();
        if (ents[3] != null)
            SixPipe.renderFront();
        if (ents[2] != null)
            SixPipe.renderBack();
        if (ents[5] != null)
            SixPipe.renderRight();
        if (ents[4] != null)
            SixPipe.renderLeft();
        SixPipe.renderMiddle();
        bindTexture(VALVE_TEXTURE);
        if (ents[1] == null)
            valve.render();
        GL11.glPopMatrix();

    }
}
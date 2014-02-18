package resonantinduction.archaic.channel;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.api.mechanical.fluid.IFluidNetwork;
import resonantinduction.api.mechanical.fluid.IFluidPipe;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.mechanical.fluid.pipe.EnumPipeMaterial;
import resonantinduction.mechanical.fluid.pipe.ModelPipe;
import resonantinduction.mechanical.fluid.pipe.PartPipe;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChannel extends TileEntitySpecialRenderer
{
    public static final RenderChannel INSTANCE = new RenderChannel();

    public static ModelChannel MODEL_TROUGH_PIPE = new ModelChannel();
    public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "white.png");

    public static void render(int meta, byte sides)
    {
        RenderUtility.bind(TEXTURE);
        MODEL_TROUGH_PIPE.render(sides, meta == 0 ? true : false);
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f)
    {
        System.out.println("Update tick channel renderer");
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        render(0, (tileentity instanceof TileChannel ? ((TileChannel) tileentity).renderSides : (byte)0));
        GL11.glPopMatrix();
    }
}
package resonantinduction.assemblyline.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.assemblyline.AssemblyLine;
import resonantinduction.assemblyline.client.model.ModelCrusher;
import resonantinduction.assemblyline.client.model.ModelGrinder;
import resonantinduction.assemblyline.machine.processor.TileEntityProcessor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderProcessor extends TileEntitySpecialRenderer
{
    private ModelCrusher crusherModel;
    private ModelGrinder grinderModel;

    public RenderProcessor()
    {
        this.crusherModel = new ModelCrusher();
        this.grinderModel = new ModelGrinder();
    }

    private void renderAModelAt(TileEntityProcessor tileEntity, double x, double y, double z, float f)
    {
        bindTexture(this.getTexture(tileEntity.getBlockType().blockID, tileEntity.getBlockMetadata()));

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);

        if (tileEntity.getDirection() == ForgeDirection.NORTH)
        {
            GL11.glRotatef(180f, 0f, 1f, 0f);
        }
        else if (tileEntity.getDirection() == ForgeDirection.SOUTH)
        {
            GL11.glRotatef(0f, 0f, 1f, 0f);
        }
        else if (tileEntity.getDirection() == ForgeDirection.WEST)
        {
            GL11.glRotatef(90f, 0f, 1f, 0f);
        }
        else if (tileEntity.getDirection() == ForgeDirection.EAST)
        {
            GL11.glRotatef(270f, 0f, 1f, 0f);
        }

        int g = tileEntity.blockMetadata / 4;
        if (g == 0)
        {
            crusherModel.renderBody(0.0625F);
            crusherModel.renderPiston(0.0625F, tileEntity.renderStage);
        }
        else if (g == 1)
        {
            grinderModel.renderBody(0.0625F);
            grinderModel.renderRotation(0.0625F, tileEntity.renderStage);
        }
        else if (g == 2)
        {

        }
        else if (g == 3)
        {

        }

        GL11.glPopMatrix();
    }

    public ResourceLocation getTexture(int block, int meta)
    {
        int g = meta / 4;
        if (g == 0)
        {
            return new ResourceLocation(AssemblyLine.DOMAIN, AssemblyLine.MODEL_DIRECTORY + "CrusherBlock.png");
        }
        else if (g == 1)
        {
            return new ResourceLocation(AssemblyLine.DOMAIN, AssemblyLine.MODEL_DIRECTORY + "GrinderBlock.png");
        }
        return null;

    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
    {
        if (tileEntity instanceof TileEntityProcessor)
            this.renderAModelAt((TileEntityProcessor) tileEntity, var2, var4, var6, var8);

    }
}
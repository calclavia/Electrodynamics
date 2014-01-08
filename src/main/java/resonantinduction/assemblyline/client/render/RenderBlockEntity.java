package resonantinduction.assemblyline.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import universalelectricity.api.vector.Vector3;

public class RenderBlockEntity extends Render
{

    public static RenderBlockEntity INSTANCE = new RenderBlockEntity();

    private RenderBlockEntity()
    {
    }

    @Override
    public void doRender(Entity entity, double i, double j, double k, float f, float f1)
    {
        doRenderBlock((EntityFakeBlock) entity, i, j, k);
    }

    public void doRenderBlock(EntityFakeBlock entity, double i, double j, double k)
    {
        if (entity.isDead)
            return;

        shadowSize = entity.shadowSize;
        World world = entity.worldObj;
        BlockRenderInfo util = new BlockRenderInfo();
        util.texture = entity.texture;
        this.bindTexture(TextureMap.locationBlocksTexture);

        for (int iBase = 0; iBase < entity.iSize; ++iBase)
        {
            for (int jBase = 0; jBase < entity.jSize; ++jBase)
            {
                for (int kBase = 0; kBase < entity.kSize; ++kBase)
                {

                    util.min = new Vector3();
                    util.max = new Vector3();

                    double remainX = entity.iSize - iBase;
                    double remainY = entity.jSize - jBase;
                    double remainZ = entity.kSize - kBase;

                    util.max.x = (remainX > 1.0 ? 1.0 : remainX);
                    util.max.y = (remainY > 1.0 ? 1.0 : remainY);
                    util.max.z = (remainZ > 1.0 ? 1.0 : remainZ);

                    GL11.glPushMatrix();
                    GL11.glTranslatef((float) i, (float) j, (float) k);
                    GL11.glRotatef(entity.rotationX, 1, 0, 0);
                    GL11.glRotatef(entity.rotationY, 0, 1, 0);
                    GL11.glRotatef(entity.rotationZ, 0, 0, 1);
                    GL11.glTranslatef(iBase, jBase, kBase);

                    int lightX, lightY, lightZ;

                    lightX = (int) (Math.floor(entity.posX) + iBase);
                    lightY = (int) (Math.floor(entity.posY) + jBase);
                    lightZ = (int) (Math.floor(entity.posZ) + kBase);

                    GL11.glDisable(2896 /* GL_LIGHTING */);
                    renderBlock(util, world, lightX, lightY, lightZ, false, true);
                    GL11.glEnable(2896 /* GL_LIGHTING */);
                    GL11.glPopMatrix();

                }
            }
        }
    }

    public void renderBlock(BlockRenderInfo block, IBlockAccess blockAccess, int x, int y, int z, boolean doLight, boolean doTessellating)
    {
        float f = 0.5F;
        float f1 = 1.0F;
        float f2 = 0.8F;
        float f3 = 0.6F;

        renderBlocks.renderMaxX = block.max.x;
        renderBlocks.renderMinX = block.min.x;

        renderBlocks.renderMaxY = block.max.y;
        renderBlocks.renderMinY = block.min.y;

        renderBlocks.renderMaxZ = block.max.z;
        renderBlocks.renderMinZ = block.min.z;

        renderBlocks.enableAO = false;

        Tessellator tessellator = Tessellator.instance;

        if (doTessellating)
        {
            tessellator.startDrawingQuads();
        }

        float f4 = 0, f5 = 0;

        if (doLight)
        {
            f4 = block.getBlockBrightness(blockAccess, x, y, z);
            f5 = block.getBlockBrightness(blockAccess, x, y, z);
            if (f5 < f4)
            {
                f5 = f4;
            }
            tessellator.setColorOpaque_F(f * f5, f * f5, f * f5);
        }

        renderBlocks.renderFaceYNeg(null, 0, 0, 0, block.getBlockTextureFromSide(0));

        if (doLight)
        {
            f5 = block.getBlockBrightness(blockAccess, x, y, z);
            if (f5 < f4)
            {
                f5 = f4;
            }
            tessellator.setColorOpaque_F(f1 * f5, f1 * f5, f1 * f5);
        }

        renderBlocks.renderFaceYPos(null, 0, 0, 0, block.getBlockTextureFromSide(1));

        if (doLight)
        {
            f5 = block.getBlockBrightness(blockAccess, x, y, z);
            if (f5 < f4)
            {
                f5 = f4;
            }
            tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
        }

        renderBlocks.renderFaceZNeg(null, 0, 0, 0, block.getBlockTextureFromSide(2));

        if (doLight)
        {
            f5 = block.getBlockBrightness(blockAccess, x, y, z);
            if (f5 < f4)
            {
                f5 = f4;
            }
            tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
        }

        renderBlocks.renderFaceZPos(null, 0, 0, 0, block.getBlockTextureFromSide(3));

        if (doLight)
        {
            f5 = block.getBlockBrightness(blockAccess, x, y, z);
            if (f5 < f4)
            {
                f5 = f4;
            }
            tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
        }

        renderBlocks.renderFaceXNeg(null, 0, 0, 0, block.getBlockTextureFromSide(4));

        if (doLight)
        {
            f5 = block.getBlockBrightness(blockAccess, x, y, z);
            if (f5 < f4)
            {
                f5 = f4;
            }
            tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
        }

        renderBlocks.renderFaceXPos(null, 0, 0, 0, block.getBlockTextureFromSide(5));

        if (doTessellating)
        {
            tessellator.draw();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

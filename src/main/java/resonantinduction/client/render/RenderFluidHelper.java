package resonantinduction.client.render;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public class RenderFluidHelper
{

    private static final ResourceLocation BLOCK_TEXTURE = TextureMap.locationBlocksTexture;

    private static Map<Fluid, int[]> flowingRenderCache = new HashMap<Fluid, int[]>();
    private static Map<Fluid, int[]> stillRenderCache = new HashMap<Fluid, int[]>();

    public static final int DISPLAY_STAGES = 100;

    private static final BlockRenderInfo liquidBlock = new BlockRenderInfo();

    public static ResourceLocation getFluidSheet(FluidStack liquid)
    {
        return BLOCK_TEXTURE;
    }

    public static Icon getFluidTexture(Fluid fluid, boolean flowing)
    {
        if (fluid == null)
        {
            return null;
        }
        Icon icon = flowing ? fluid.getFlowingIcon() : fluid.getStillIcon();
        if (icon == null)
        {
            icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
        }
        return icon;
    }

    public static void setColorForFluidStack(FluidStack fluidstack)
    {
        if (fluidstack == null)
        {
            return;
        }

        int color = fluidstack.getFluid().getColor(fluidstack);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, 1);
    }

    public static int[] getFluidDisplayLists(FluidStack fluidStack, World world, boolean flowing)
    {
        if (fluidStack == null)
        {
            return null;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null)
        {
            return null;
        }
        Map<Fluid, int[]> cache = flowing ? flowingRenderCache : stillRenderCache;
        int[] diplayLists = cache.get(fluid);
        if (diplayLists != null)
        {
            return diplayLists;
        }

        diplayLists = new int[DISPLAY_STAGES];

        if (fluid.getBlockID() > 0)
        {
            liquidBlock.baseBlock = Block.blocksList[fluid.getBlockID()];
            liquidBlock.texture = getFluidTexture(fluidStack.getFluid(), flowing);
        }
        else
        {
            liquidBlock.baseBlock = Block.waterStill;
            liquidBlock.texture = getFluidTexture(fluidStack.getFluid(), flowing);
        }

        cache.put(fluid, diplayLists);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        for (int s = 0; s < DISPLAY_STAGES; ++s)
        {
            diplayLists[s] = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(diplayLists[s], 4864 /*GL_COMPILE*/);

            liquidBlock.min.x = 0.01f;
            liquidBlock.min.y = 0;
            liquidBlock.min.z = 0.01f;

            liquidBlock.max.x = 0.99f;
            liquidBlock.max.y = (float) s / (float) DISPLAY_STAGES;
            liquidBlock.max.z = 0.99f;

            RenderBlockEntity.INSTANCE.renderBlock(liquidBlock, world, 0, 0, 0, false, true);

            GL11.glEndList();
        }

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);

        return diplayLists;
    }
}

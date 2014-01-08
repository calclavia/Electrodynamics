package resonantinduction.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.AssemblyLine;
import resonantinduction.client.model.ModelSmallFluidCan;
import resonantinduction.core.recipe.RecipeLoader;
import resonantinduction.transport.fluid.ItemFluidCan;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderFluidCan implements IItemRenderer
{
    public static final ModelSmallFluidCan CAN_MODEL = new ModelSmallFluidCan();

    public static final ResourceLocation CAN_TEXTURE = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "FluidCanA.png");

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        if (RecipeLoader.itemFluidCan != null && item.itemID == RecipeLoader.itemFluidCan.itemID)
        {
            FluidStack liquid = ((ItemFluidCan) RecipeLoader.itemFluidCan).drain(item, Integer.MAX_VALUE, false);

            if (liquid != null && liquid.amount > 100)
            {

                int[] displayList = RenderFluidHelper.getFluidDisplayLists(liquid, Minecraft.getMinecraft().theWorld, false);

                GL11.glPushMatrix();
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                GL11.glScalef(0.4F, 1.5F, 0.4F);

                if (type == ItemRenderType.ENTITY)
                {
                    GL11.glTranslatef(-.5F, 0F, -.5F);
                }
                else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
                {
                    GL11.glTranslatef(1.8F, 0.25F, 1.8F);
                }
                else if (type == ItemRenderType.EQUIPPED)
                {
                    GL11.glTranslatef(0.9F, 0.4F, 1.2F);
                }
                else
                {
                    GL11.glTranslatef(0.6F, 0F, 0.6F);
                }

                FMLClientHandler.instance().getClient().renderEngine.bindTexture((RenderFluidHelper.getFluidSheet(liquid)));

                int cap = ((ItemFluidCan) RecipeLoader.itemFluidCan).getCapacity(item);
                if (liquid.getFluid().isGaseous())
                {
                    cap = liquid.amount;
                }
                GL11.glCallList(displayList[(int) ((float) liquid.amount / (float) (cap) * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }
            GL11.glPushMatrix();

            FMLClientHandler.instance().getClient().renderEngine.bindTexture(CAN_TEXTURE);
            float scale = 1.8f;
            GL11.glScalef(scale, scale, scale);
            if (type == ItemRenderType.ENTITY)
            {
                GL11.glTranslatef(0F, -1F, 0F);
            }
            else if (type == ItemRenderType.EQUIPPED)
            {
                GL11.glTranslatef(0.3F, -0.7F, 0.37F);
            }
            else
            {
                GL11.glTranslatef(0.5F, -0.8F, 0.5F);
            }
            CAN_MODEL.render(0.0625F);
            GL11.glPopMatrix();
        }

    }
}

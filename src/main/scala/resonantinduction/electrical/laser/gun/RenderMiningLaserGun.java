package resonantinduction.electrical.laser.gun;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMiningLaserGun implements IItemRenderer
{
    public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "MiningLaserGun.tcn");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "LaserGun.png");

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
        GL11.glPushMatrix();

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
        {
            float scale = 5f;

            if (Minecraft.getMinecraft().thePlayer.getItemInUse() != item)
            {
                GL11.glScalef(scale, scale, scale);
                GL11.glTranslatef(0.2f, 0.2f, 0.67f);
                GL11.glRotatef(-40, 0, 1, 0);
                GL11.glRotatef(10, 1, 0, 0);
            }
            else
            {
                GL11.glScalef(scale, scale, scale);
                GL11.glTranslatef(0.2f, 0.2f, 0.67f);
                GL11.glRotatef(-40, 0, 1, 0);
                GL11.glRotatef(20, 1, 0, 0);
            }
        }
        else if (type == ItemRenderType.EQUIPPED)
        {
            float scale = 3f;
            GL11.glScalef(scale, scale, scale);
            GL11.glRotatef(-80, 1, 0, 0);
            GL11.glRotatef(60, 0, 1, 0);
            GL11.glRotatef(20, 0, 0, 1);
            GL11.glTranslatef(0f, 0f, 0.6f);
        }
        else if (type == ItemRenderType.INVENTORY)
        {
            float scale = 1.5f;
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(0.1f, 0.06f, 0.52f);
        }
        MODEL.renderAll();

        GL11.glPopMatrix();
    }
}

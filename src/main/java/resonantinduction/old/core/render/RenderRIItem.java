package resonantinduction.old.core.render;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import resonantinduction.old.electrical.multimeter.ItemMultimeter;
import resonantinduction.old.electrical.multimeter.RenderMultimeter;
import resonantinduction.old.energy.transformer.ItemTransformer;
import resonantinduction.old.energy.transformer.RenderTransformer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderRIItem implements IItemRenderer
{
	public static final RenderRIItem INSTANCE = new RenderRIItem();

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
		if (item.getItem() instanceof ItemMultimeter)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glTranslated(0, -1, -0.7);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderMultimeter.TEXTURE);
			RenderMultimeter.MODEL.render(0.0625f);
			GL11.glPopMatrix();
		}
		else if (item.getItem() instanceof ItemTransformer)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(180, 0, 0, 1);
			GL11.glTranslated(0, -1, 0);
			GL11.glScaled(0.9f, 0.9f, 0.9f);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderTransformer.TEXTURE);
			RenderTransformer.MODEL.render(0.0625f);
			GL11.glPopMatrix();
		}
	}

}

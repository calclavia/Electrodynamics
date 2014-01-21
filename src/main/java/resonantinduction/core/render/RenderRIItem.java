package resonantinduction.core.render;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import resonantinduction.electrical.battery.ItemBlockBattery;
import resonantinduction.electrical.battery.RenderBattery;
import resonantinduction.electrical.multimeter.ItemMultimeter;
import resonantinduction.electrical.multimeter.RenderMultimeter;
import resonantinduction.electrical.transformer.ItemTransformer;
import resonantinduction.electrical.transformer.RenderTransformer;
import resonantinduction.mechanical.gear.ItemGear;
import resonantinduction.mechanical.gear.RenderGear;
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
		GL11.glPushMatrix();

		if (item.getItem() instanceof ItemBlockBattery)
		{
			RenderBattery.INSTANCE.renderInventory(type, item, data);
		}
		else if (item.getItem() instanceof ItemGear)
		{
			RenderGear.INSTANCE.renderInventory(null, 0, 0, null);
		}
		else if (item.getItem() instanceof ItemMultimeter)
		{
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glTranslated(0, -1, -0.7);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderMultimeter.TEXTURE);
			RenderMultimeter.MODEL.render(0.0625f);
		}
		else if (item.getItem() instanceof ItemTransformer)
		{
			GL11.glTranslated(0, -0.2f, 0);
			RenderTransformer.INSTANCE.doRender();
		}

		GL11.glPopMatrix();
	}

}

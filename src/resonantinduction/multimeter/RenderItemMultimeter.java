package resonantinduction.multimeter;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderItemMultimeter implements IItemRenderer
{
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return item.getItem() instanceof ItemMultimeter;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return item.getItem() instanceof ItemMultimeter;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();
		GL11.glRotatef(180, 0, 1, 0);
		GL11.glTranslated(0, -1, -0.7);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderMultimeter.TEXTURE);
		RenderMultimeter.MODEL.render(0.0625f);
		GL11.glPopMatrix();
	}

}

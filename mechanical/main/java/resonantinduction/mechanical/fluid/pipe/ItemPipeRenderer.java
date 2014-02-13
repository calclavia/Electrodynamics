package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemPipeRenderer implements IItemRenderer
{
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
		GL11.glRotatef(180f, 0f, 0f, 1f);
		int meta = item.getItemDamage();

		if (type == ItemRenderType.ENTITY)
		{
			GL11.glTranslatef(-.5F, -1F, -.5F);
			RenderPipe.render(meta, Byte.parseByte("001100", 2));
		}
		else if (type == ItemRenderType.INVENTORY)
		{
			GL11.glTranslatef(0F, -1F, 0F);
			RenderPipe.render(meta, Byte.parseByte("001100", 2));
		}
		else if (type == ItemRenderType.EQUIPPED)
		{
			GL11.glTranslatef(-1F, -1.2F, 0.5F);
			RenderPipe.render(meta, Byte.parseByte("000011", 2));
		}
		else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glTranslatef(-2F, -1.5F, 0.2F);
			RenderPipe.render(meta, Byte.parseByte("000011", 2));
		}
		else
		{
			RenderPipe.render(item.getItemDamage(), Byte.parseByte("000011", 2));
		}

		GL11.glPopMatrix();
	}
}

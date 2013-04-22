package mffs.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public class GuiIcon extends GuiButton
{
	public static RenderItem itemRenderer = new RenderItem();
	public ItemStack itemStack;

	public GuiIcon(int par1, int par2, int par3, ItemStack itemStack)
	{
		super(par1, par2, par3, 20, 20, "");
		this.itemStack = itemStack;
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		super.drawButton(par1Minecraft, par2, par3);

		if (this.drawButton)
		{
			this.drawItemStack(this.itemStack, this.xPosition, this.yPosition);
		}
	}

	protected void drawItemStack(ItemStack itemStack, int x, int y)
	{
		x += 2;
		y -= 1;

		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer fontRenderer = mc.fontRenderer;

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		this.zLevel = 500.0F;
		itemRenderer.zLevel = 500.0F;
		itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, itemStack, x, y);
		itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, itemStack, x, y);
		this.zLevel = 0.0F;
		itemRenderer.zLevel = 0.0F;
		RenderHelper.disableStandardItemLighting();
	}

}

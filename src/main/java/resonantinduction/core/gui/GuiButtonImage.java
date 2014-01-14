package resonantinduction.core.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonImage extends GuiButton
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.GUI_DIRECTORY + "gui_button.png");

	private ButtonIcon buttonIcon = ButtonIcon.BLANK;

	public GuiButtonImage(int buttonID, int xx, int yy, ButtonIcon icon)
	{
		super(buttonID, xx, yy, 20, 20, "");
		this.buttonIcon = icon;
		this.width = icon.sizeX;
		this.height = icon.sizeY;
	}

	/** Draws this button to the screen. */
	@Override
	public void drawButton(Minecraft mc, int width, int hight)
	{
		if (this.drawButton)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			boolean hovering = width >= this.xPosition && hight >= this.yPosition && width < this.xPosition + this.width && hight < this.yPosition + this.height;
			int vv = buttonIcon.vv;
			int uu = buttonIcon.uu;
			if (hovering)
			{
				vv += this.height;
			}

			this.drawTexturedModalRect(this.xPosition, this.yPosition, this.buttonIcon.uu, this.buttonIcon.vv, this.width, this.height);
		}
	}

	/** Checks to see if the x and y coords are intersecting with the button. */
	public boolean isIntersect(int x, int y)
	{
		return x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
	}

	public static enum ButtonIcon
	{
		PERSON(0, 0), ARROW_LEFT(30, 0, 10, 10), ARROW_RIGHT(20, 0, 10, 10),
		ARROW_DOWN(30, 20, 10, 10), ARROW_UP(20, 20, 10, 10), CHEST(60, 0), LOCKED(80, 0),
		UNLOCKED(100, 0), BLANK(120, 0), RED_ON(140, 0), RED_OFF(160, 0), FURNACE_OFF(180, 0),
		FURNACE_ON(200, 0);

		int vv, uu;
		int sizeX = 20, sizeY = 20;

		private ButtonIcon(int xx, int yy)
		{
			this.vv = yy;
			this.uu = xx;
		}

		private ButtonIcon(int xx, int yy, int cx, int cy)
		{
			this(xx, yy);
			this.sizeX = cx;
			this.sizeY = cy;
		}
	}
}
package mffs.gui.button;

import mffs.ModularForceFieldSystem;
import mffs.base.GuiBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector2;
import universalelectricity.prefab.TranslationHelper;

public class GuiButtonPress extends GuiButton
{
	protected Vector2 offset = new Vector2();

	/**
	 * Stuck determines if the button is hard pressed done, or disabled looking.
	 */
	public boolean stuck = false;
	private int type;
	private GuiBase mainGui;
	private String name;

	public GuiButtonPress(int id, int x, int y, Vector2 offset, GuiBase mainGui, String name)
	{
		super(id, x, y, 18, 18, name);
		this.offset = offset;
		this.mainGui = mainGui;
	}

	public GuiButtonPress(int id, int x, int y, Vector2 offset, GuiBase mainGui)
	{
		this(id, x, y, offset, mainGui, "");
	}

	public GuiButtonPress(int id, int x, int y, Vector2 offset)
	{
		this(id, x, y, offset, null, "");
	}

	public GuiButtonPress(int id, int x, int y)
	{
		this(id, x, y, new Vector2());
	}

	@Override
	public void drawButton(Minecraft minecraft, int x, int y)
	{
		if (this.drawButton)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(ModularForceFieldSystem.GUI_BUTTON);

			if (this.stuck)
			{
				GL11.glColor4f(0.6f, 0.6f, 0.6f, 1);
			}
			else if (this.isPointInRegion(this.xPosition, this.yPosition, this.width, this.height, x, y))
			{
				GL11.glColor4f(0.85f, 0.85f, 0.85f, 1);
			}
			else
			{
				GL11.glColor4f(1, 1, 1, 1);
			}

			this.drawTexturedModalRect(this.xPosition, this.yPosition, this.offset.intX(), this.offset.intY(), this.width, this.height);
			this.mouseDragged(minecraft, x, y);
		}
	}

	@Override
	protected void mouseDragged(Minecraft minecraft, int x, int y)
	{
		if (this.mainGui != null && this.displayString != null && this.displayString.length() > 0)
		{
			if (this.isPointInRegion(this.xPosition, this.yPosition, this.width, this.height, x, y))
			{
				String title = TranslationHelper.getLocal("gui." + this.displayString + ".name");

				this.mainGui.tooltip = TranslationHelper.getLocal("gui." + this.displayString + ".tooltip");

				if (title != null && title.length() > 0)
				{
					this.mainGui.tooltip = title + ": " + this.mainGui.tooltip;
				}
			}
		}
	}

	protected boolean isPointInRegion(int x, int y, int width, int height, int checkX, int checkY)
	{
		int var7 = 0;
		int var8 = 0;
		checkX -= var7;
		checkY -= var8;
		return checkX >= x - 1 && checkX < x + width + 1 && checkY >= y - 1 && checkY < y + height + 1;
	}
}
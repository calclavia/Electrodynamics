package resonantinduction.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.api.coding.IRedirectTask;
import resonantinduction.api.coding.ITask;
import resonantinduction.transport.ResonantInductionTransport;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTask extends Gui
{
	boolean isLeft = false;
	private ResourceLocation gui_pic = new ResourceLocation(ResonantInductionTransport.GUI_DIRECTORY + "gui@.png");
	/** Button width in pixels */
	protected int width;

	/** Button height in pixels */
	protected int height;

	/** The x position of this control. */
	public int xPosition;

	/** The y position of this control. */
	public int yPosition;
	ITask task;

	/** True if this control is enabled, false to disable. */
	public boolean enabled;

	/** Hides the button completely if false. */
	public boolean drawButton;
	protected boolean field_82253_i;

	public GuiTask(int x, int y, ITask task)
	{
		this.xPosition = x;
		this.yPosition = y;
		this.task = task;
		this.width = 50;
		this.height = 50;
		this.drawButton = true;
		if (task instanceof IRedirectTask)
		{
			this.drawButton = ((IRedirectTask) task).render();
		}
		switch (task.getType())
		{
			case DATA:
				gui_pic = new ResourceLocation(ResonantInductionTransport.GUI_DIRECTORY + "logic/DATA.png");
				break;
			case PROCESS:
				gui_pic = new ResourceLocation(ResonantInductionTransport.GUI_DIRECTORY + "logic/PROCESS.png");
				break;
			case DEFINEDPROCESS:
				gui_pic = new ResourceLocation(ResonantInductionTransport.GUI_DIRECTORY + "logic/DEFINEDPROCESS.png");
				break;
			case DECISION:
				gui_pic = new ResourceLocation(ResonantInductionTransport.GUI_DIRECTORY + "logic/IF.png");
				break;
		}
	}

	public void drawTask(Minecraft par1Minecraft, int par2, int par3)
	{
		if (this.drawButton)
		{
			FontRenderer fontrenderer = par1Minecraft.fontRenderer;
			par1Minecraft.getTextureManager().bindTexture(gui_pic);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.field_82253_i = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
			int k = this.getHoverState(this.field_82253_i);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
			this.mouseDragged(par1Minecraft, par2, par3);
			int l = 14737632;

			if (!this.enabled)
			{
				l = -6250336;
			}
			else if (this.field_82253_i)
			{
				l = 16777120;
			}

			this.drawCenteredString(fontrenderer, this.task.getMethodName(), this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
		}
	}

	/**
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if
	 * it IS hovering over this button.
	 */
	protected int getHoverState(boolean par1)
	{
		byte b0 = 1;

		if (!this.enabled)
		{
			b0 = 0;
		}
		else if (par1)
		{
			b0 = 2;
		}

		return b0;
	}

	/**
	 * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent
	 * e).
	 */
	protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3)
	{
	}

	/**
	 * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent
	 * e).
	 */
	public void mouseReleased(int par1, int par2)
	{
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of
	 * MouseListener.mousePressed(MouseEvent e).
	 */
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3)
	{
		return this.enabled && this.drawButton && par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
	}

	public boolean func_82252_a()
	{
		return this.field_82253_i;
	}

	public void func_82251_b(int par1, int par2)
	{
	}
}
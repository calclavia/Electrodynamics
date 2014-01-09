package com.builtbroken.minecraft.prefab.invgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class GuiMessageBox extends GuiBase
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(DarkCore.DOMAIN, DarkCore.GUI_DIRECTORY + "gui_message_box.png");

	GuiBase returnGuiYes, returnGuiNo;
	int id;
	String title;
	String message;

	public GuiMessageBox(GuiBase returnGui, int id, String title, String message)
	{
		this.guiSize.y = 380 / 2;
		this.returnGuiYes = returnGui;
		this.returnGuiNo = returnGui;
		this.id = id;
		this.title = title;
		this.message = message;
	}

	public GuiMessageBox(GuiBase returnGui, GuiBase returnGuiNo, int id, String title, String message)
	{
		this(returnGui, id, title, message);
		this.returnGuiNo = returnGuiNo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, (this.width - this.guiSize.intX()) / 2 + 25, (this.height - this.guiSize.intY()) / 2 + 35, 50, 20, "Yes"));

		this.buttonList.add(new GuiButton(1, (this.width - this.guiSize.intX()) / 2 + 80, (this.height - this.guiSize.intY()) / 2 + 35, 50, 20, "no"));

	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		switch (button.id)
		{
			case 0:
				this.exit(true);
				break;
			case 1:
				this.exit(false);
				break;
		}
	}

	@Override
	protected void drawForegroundLayer(int var2, int var3, float var1)
	{
		this.fontRenderer.drawString("" + this.title, (this.guiSize.intX() / 2 - 30), 5, 4210752);
		this.fontRenderer.drawString("\u00a77" + this.message, ((this.guiSize.intX() / 2) - 50), 20, 4210752);
	}

	@Override
	protected void drawBackgroundLayer(int var2, int var3, float var1)
	{
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int containerWidth = (this.width - this.guiSize.intX()) / 2;
		int containerHeight = (this.height - this.guiSize.intY()) / 2;
		this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.guiSize.intX(), this.guiSize.intY());
	}

	public void show()
	{
		FMLCommonHandler.instance().showGuiScreen(this);
	}

	public void exit(boolean yes)
	{
		if (yes)
		{
			FMLCommonHandler.instance().showGuiScreen(this.returnGuiYes);
		}
		else
		{
			FMLCommonHandler.instance().showGuiScreen(this.returnGuiNo);
		}
		if (this.returnGuiYes instanceof IMessageBoxDialog)
		{
			((IMessageBoxDialog) this.returnGuiYes).onMessageBoxClosed(this.id, yes);
		}
	}
}

package resonantinduction.old.lib.prefab.invgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import resonantinduction.core.Reference;
import resonantinduction.core.prefab.tile.TileEntityMachine;
import resonantinduction.old.lib.prefab.invgui.GuiButtonImage.ButtonIcon;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiMachineContainer extends GuiContainer
{
	protected static final int MAX_BUTTON_ID = 3;
	protected TileEntityMachine tileEntity;
	protected EntityPlayer entityPlayer;
	protected Object mod;
	protected int guiID = -1, guiID2 = -1, guiID3 = -1;
	protected ButtonIcon guiIcon = ButtonIcon.CHEST, guiIcon2 = ButtonIcon.PERSON,
			guiIcon3 = ButtonIcon.BLANK;
	protected String invName = "Home", invName2 = "2", invName3 = "3";

	public ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.GUI_DIRECTORY + "gui_grey.png");

	protected int containerWidth;
	protected int containerHeight;

	public GuiMachineContainer(Object mod, Container container, InventoryPlayer inventoryPlayer, TileEntityMachine tileEntity)
	{
		super(container);
		this.tileEntity = tileEntity;
		this.entityPlayer = inventoryPlayer.player;
		this.ySize = 380 / 2;
		this.mod = mod;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		containerWidth = (this.width - this.xSize) / 2;
		containerHeight = (this.height - this.ySize) / 2;
		if (guiID != -1)
			this.buttonList.add(new GuiButtonImage(0, containerWidth - 22, containerHeight + 0, guiIcon));
		if (guiID2 != -1)
			this.buttonList.add(new GuiButtonImage(1, containerWidth - 22, containerHeight + 22, guiIcon2));
		if (guiID3 != -1)
			this.buttonList.add(new GuiButtonImage(2, containerWidth - 22, containerHeight + 44, guiIcon3));

	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		switch (button.id)
		{
			case 0:
			{
				if (guiID != -1)
					this.entityPlayer.openGui(mod, guiID, this.tileEntity.worldObj, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord);
				break;
			}
			case 1:
			{
				if (guiID2 != -1)
					this.entityPlayer.openGui(mod, guiID2, this.tileEntity.worldObj, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord);
				break;
			}
			case 2:
			{
				if (guiID3 != -1)
					this.entityPlayer.openGui(mod, guiID3, this.tileEntity.worldObj, this.tileEntity.xCoord, this.tileEntity.yCoord, this.tileEntity.zCoord);
				break;
			}
		}
	}

	/** Draw the foreground layer for the GuiContainer (everything in front of the items) */
	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString("\u00a77" + tileEntity.getInvName(), (int) (this.xSize / 2 - 7 * 2.5), 4, 4210752);

		/** Render Tool Tips */
		if (((GuiButtonImage) this.buttonList.get(0)).isIntersect(x, y))
		{
			this.drawTooltip(x - this.guiLeft, y - this.guiTop + 10, invName);
		}
		else if (((GuiButtonImage) this.buttonList.get(1)).isIntersect(x, y))
		{
			this.drawTooltip(x - this.guiLeft, y - this.guiTop + 10, invName2);
		}
		else if (((GuiButtonImage) this.buttonList.get(2)).isIntersect(x, y))
		{
			this.drawTooltip(x - this.guiLeft, y - this.guiTop + 10, invName3);
		}
	}

	/** Draw the background layer for the GuiContainer (everything behind the items) */
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int x, int y)
	{
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int containerWidth = (this.width - this.xSize) / 2;
		int containerHeight = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);
	}

	public void drawTooltip(int x, int y, String... toolTips)
	{
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		if (toolTips != null)
		{
			int var5 = 0;
			int var6;
			int var7;

			for (var6 = 0; var6 < toolTips.length; ++var6)
			{
				var7 = this.fontRenderer.getStringWidth(toolTips[var6]);

				if (var7 > var5)
				{
					var5 = var7;
				}
			}

			var6 = x + 12;
			var7 = y - 12;
			int var9 = 8;

			if (toolTips.length > 1)
			{
				var9 += 2 + (toolTips.length - 1) * 10;
			}

			if (this.guiTop + var7 + var9 + 6 > this.height)
			{
				var7 = this.height - var9 - this.guiTop - 6;
			}

			this.zLevel = 300.0F;
			int var10 = -267386864;
			this.drawGradientRect(var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10);
			this.drawGradientRect(var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10);
			this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10);
			this.drawGradientRect(var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10);
			this.drawGradientRect(var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10);
			int var11 = 1347420415;
			int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
			this.drawGradientRect(var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12);
			this.drawGradientRect(var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12);
			this.drawGradientRect(var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11);
			this.drawGradientRect(var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12);

			for (int var13 = 0; var13 < toolTips.length; ++var13)
			{
				String var14 = "\u00a77" + toolTips[var13];

				this.fontRenderer.drawStringWithShadow(var14, var6, var7, -1);

				if (var13 == 0)
				{
					var7 += 2;
				}

				var7 += 10;
			}

			this.zLevel = 0.0F;
		}
	}

	public int getGuiTop()
	{
		return this.guiTop;
	}

	public int getGuiLeft()
	{
		return this.guiLeft;
	}

	public TileEntity getTile()
	{
		return this.tileEntity;
	}
}

package mffs.base;

import icbm.api.IBlockFrequency;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.IBiometricIdentifierLink;
import mffs.base.TileEntityBase.TilePacketType;
import mffs.gui.button.GuiIcon;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector2;
import universalelectricity.prefab.TranslationHelper;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.vector.Region2;
import calclavia.lib.Calclavia;
import calclavia.lib.gui.GuiContainerBase;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiMFFS extends GuiContainerBase
{
	public enum SlotType
	{
		NONE, BATTERY, LIQUID, ARR_UP, ARR_DOWN, ARR_LEFT, ARR_RIGHT, ARR_UP_RIGHT, ARR_UP_LEFT,
		ARR_DOWN_LEFT, ARR_DOWN_RIGHT
	}

	protected GuiTextField textFieldFrequency;
	protected Vector2 textFieldPos = new Vector2();
	public String tooltip = "";
	protected IBlockFrequency frequencyTile;

	protected HashMap<Region2, String> tooltips = new HashMap<Region2, String>();

	public GuiMFFS(Container container)
	{
		super(container);
		this.ySize = 217;
	}

	public GuiMFFS(Container container, IBlockFrequency frequencyTile)
	{
		this(container);
		this.frequencyTile = frequencyTile;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiIcon(0, this.width / 2 - 82, this.height / 2 - 104, new ItemStack(Block.torchRedstoneIdle), new ItemStack(Block.torchRedstoneActive)));

		Keyboard.enableRepeatEvents(true);

		if (this.frequencyTile != null)
		{
			this.textFieldFrequency = new GuiTextField(this.fontRenderer, this.textFieldPos.intX(), this.textFieldPos.intY(), 50, 12);
			this.textFieldFrequency.setMaxStringLength(Settings.MAX_FREQUENCY_DIGITS);
			this.textFieldFrequency.setText(frequencyTile.getFrequency() + "");
		}
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		super.onGuiClosed();
	}

	@Override
	protected void keyTyped(char par1, int par2)
	{
		super.keyTyped(par1, par2);

		if (this.textFieldFrequency != null)
		{
			/**
			 * Every time a key is typed, try to reset the frequency.
			 */
			this.textFieldFrequency.textboxKeyTyped(par1, par2);

			try
			{
				int newFrequency = Math.max(0, Integer.parseInt(this.textFieldFrequency.getText()));
				this.frequencyTile.setFrequency(newFrequency);
				this.textFieldFrequency.setText(this.frequencyTile.getFrequency() + "");
				PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, (TileEntity) this.frequencyTile, TilePacketType.FREQUENCY.ordinal(), this.frequencyTile.getFrequency()));
			}
			catch (NumberFormatException e)
			{
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		super.actionPerformed(guiButton);

		if (this.frequencyTile != null && guiButton.id == 0)
		{
			PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, (TileEntity) this.frequencyTile, TilePacketType.TOGGLE_ACTIVATION.ordinal()));
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if (this.textFieldFrequency != null)
		{
			if (!this.textFieldFrequency.isFocused())
			{
				this.textFieldFrequency.setText(this.frequencyTile.getFrequency() + "");
			}
		}

		if (this.frequencyTile instanceof TileEntityBase)
		{
			if (this.buttonList.size() > 0 && this.buttonList.get(0) != null)
			{
				((GuiIcon) this.buttonList.get(0)).setIndex(((TileEntityBase) this.frequencyTile).isActive() ? 1 : 0);
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int par3)
	{
		super.mouseClicked(x, y, par3);

		if (this.textFieldFrequency != null)
		{
			this.textFieldFrequency.mouseClicked(x - this.containerWidth, y - this.containerHeight, par3);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		if (this.textFieldFrequency != null)
		{
			if (this.isPointInRegion(textFieldPos.intX(), textFieldPos.intY(), this.textFieldFrequency.getWidth(), 12, mouseX, mouseY))
			{
				this.tooltip = TranslationHelper.getLocal("gui.frequency.tooltip");
			}
		}

		Iterator<Entry<Region2, String>> it = this.tooltips.entrySet().iterator();

		while (it.hasNext())
		{
			Entry<Region2, String> entry = it.next();

			if (entry.getKey().isIn(new Vector2(mouseX - this.guiLeft, mouseY - this.guiTop)))
			{
				this.tooltip = entry.getValue();
				break;
			}
		}

		if (this.tooltip != null && this.tooltip != "")
		{
			this.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, MFFSHelper.splitStringPerWord(this.tooltip, 5).toArray(new String[] {}));
		}

		this.tooltip = "";

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(var1, x, y);

		if (this.frequencyTile instanceof IBiometricIdentifierLink)
		{
			this.drawBulb(167, 4, ((IBiometricIdentifierLink) this.frequencyTile).getBiometricIdentifier() != null);
		}
	}

	protected void drawTextWithTooltip(String textName, String format, int x, int y, int mouseX, int mouseY)
	{
		this.drawTextWithTooltip(textName, format, x, y, mouseX, mouseY, 4210752);
	}

	protected void drawTextWithTooltip(String textName, String format, int x, int y, int mouseX, int mouseY, int color)
	{
		String name = TranslationHelper.getLocal("gui." + textName + ".name");
		String text = format.replaceAll("%1", name);
		this.fontRenderer.drawString(text, x, y, color);

		String tooltip = TranslationHelper.getLocal("gui." + textName + ".tooltip");

		if (tooltip != null && tooltip != "")
		{
			if (this.isPointInRegion(x, y, (int) (text.length() * 4.8), 12, mouseX, mouseY))
			{
				this.tooltip = tooltip;
			}
		}
	}

	protected void drawTextWithTooltip(String textName, int x, int y, int mouseX, int mouseY)
	{
		this.drawTextWithTooltip(textName, "%1", x, y, mouseX, mouseY);
	}

	protected void drawSlot(int x, int y, SlotType type, float r, float g, float b)
	{
		this.mc.renderEngine.func_110577_a(Calclavia.GUI_COMPONENTS);
		GL11.glColor4f(r, g, b, 1.0F);

		this.drawTexturedModalRect(this.containerWidth + x, this.containerHeight + y, 0, 0, 18, 18);

		if (type != SlotType.NONE)
		{
			this.drawTexturedModalRect(this.containerWidth + x, this.containerHeight + y, 0, 18 * type.ordinal(), 18, 18);
		}
	}

	protected void drawSlot(int x, int y, SlotType type)
	{
		this.drawSlot(x, y, type, 1, 1, 1);
	}

	protected void drawSlot(int x, int y)
	{
		this.drawSlot(x, y, SlotType.NONE);
	}
}

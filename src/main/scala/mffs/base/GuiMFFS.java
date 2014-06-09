package mffs.base;

import calclavia.api.mffs.IBiometricIdentifierLink;
import cpw.mods.fml.common.network.PacketDispatcher;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.base.TileMFFS.TilePacketType;
import mffs.gui.button.GuiIcon;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.input.Keyboard;
import resonant.api.blocks.IBlockFrequency;
import resonant.lib.gui.GuiContainerBase;
import resonant.lib.utility.LanguageUtility;
import universalelectricity.api.vector.Vector2;

public class GuiMFFS extends GuiContainerBase
{
	/**
	 * Frequency Text Field
	 */
	protected GuiTextField textFieldFrequency;
	protected Vector2 textFieldPos = new Vector2();
	protected IBlockFrequency frequencyTile;

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
		// Redstone config button.
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
				PacketDispatcher.sendPacketToServer(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) this.frequencyTile, TilePacketType.FREQUENCY.ordinal(), this.frequencyTile.getFrequency()));
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
			PacketDispatcher.sendPacketToServer(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) this.frequencyTile, TilePacketType.TOGGLE_ACTIVATION.ordinal()));
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

		if (this.frequencyTile instanceof TileMFFS)
		{
			if (this.buttonList.size() > 0 && this.buttonList.get(0) != null)
			{
				((GuiIcon) this.buttonList.get(0)).setIndex(((TileMFFS) this.frequencyTile).isRedstoneActive ? 1 : 0);
			}
		}
	}

	@Override
	public void mouseClicked(int x, int y, int par3)
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
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		if (this.textFieldFrequency != null)
		{
			if (this.isPointInRegion(textFieldPos.intX(), textFieldPos.intY(), this.textFieldFrequency.getWidth(), 12, mouseX, mouseY))
			{
				this.tooltip = LanguageUtility.getLocal("gui.frequency.tooltip");
			}
		}
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
}

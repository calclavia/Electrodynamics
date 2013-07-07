package mffs.gui;

import mffs.ModularForceFieldSystem;
import mffs.base.GuiMFFS;
import mffs.base.TileEntityBase.TilePacketType;
import mffs.container.ContainerInterdictionMatrix;
import mffs.tileentity.TileEntityInterdictionMatrix;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.vector.Vector2;
import universalelectricity.prefab.network.PacketManager;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiInterdictionMatrix extends GuiMFFS
{
	private TileEntityInterdictionMatrix tileEntity;

	public GuiInterdictionMatrix(EntityPlayer player, TileEntityInterdictionMatrix tileEntity)
	{
		super(new ContainerInterdictionMatrix(player, tileEntity), tileEntity);
		this.tileEntity = tileEntity;
	}

	@Override
	public void initGui()
	{
		this.textFieldPos = new Vector2(110, 91);
		super.initGui();

		// Inverse Button
		this.buttonList.add(new GuiButton(1, this.width / 2 - 80, this.height / 2 - 65, 50, 20, "Banned"));
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		super.actionPerformed(guiButton);

		if (guiButton.id == 1)
		{
			PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this.tileEntity, TilePacketType.TOGGLE_MODE.ordinal()));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), this.xSize / 2 - this.fontRenderer.getStringWidth(this.tileEntity.getInvName()) / 2, 6, 4210752);

		this.drawTextWithTooltip("warn", "%1: " + this.tileEntity.getWarningRange(), 35, 19, x, y);
		this.drawTextWithTooltip("action", "%1: " + this.tileEntity.getActionRange(), 100, 19, x, y);

		this.drawTextWithTooltip("filterMode", "%1:", 9, 32, x, y);

		if (!this.tileEntity.isBanMode())
		{
			if (this.buttonList.get(1) instanceof GuiButton)
			{
				((GuiButton) this.buttonList.get(1)).displayString = "Allowed";
			}
		}
		else
		{
			if (this.buttonList.get(1) instanceof GuiButton)
			{
				((GuiButton) this.buttonList.get(1)).displayString = "Banned";
			}
		}

		this.drawTextWithTooltip("frequency", "%1:", 8, 93, x, y);
		this.textFieldFrequency.drawTextBox();

		this.drawTextWithTooltip("fortron", "%1: " + ElectricityDisplay.getDisplayShort(this.tileEntity.getFortronEnergy(), ElectricUnit.JOULES) + "/" + ElectricityDisplay.getDisplayShort(this.tileEntity.getFortronCapacity(), ElectricUnit.JOULES), 8, 110, x, y);
		this.fontRenderer.drawString("\u00a74-" + ElectricityDisplay.getDisplayShort(this.tileEntity.getFortronCost() * 20, ElectricUnit.JOULES), 120, 121, 4210752);
		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(var1, x, y);

		/**
		 * Module Slots
		 */
		for (int var3 = 0; var3 < 2; var3++)
		{
			for (int var4 = 0; var4 < 4; var4++)
			{
				this.drawSlot(98 + var4 * 18, 30 + var3 * 18);
			}
		}

		/**
		 * Item Filter Slots
		 */
		for (int var4 = 0; var4 < 9; var4++)
		{
			if (this.tileEntity.isBanMode())
			{
				this.drawSlot(8 + var4 * 18, 68, SlotType.NONE, 1f, 0.8f, 0.8f);
			}
			else
			{
				this.drawSlot(8 + var4 * 18, 68, SlotType.NONE, 0.8f, 1f, 0.8f);
			}
		}

		/**
		 * Frequency Card Slot
		 */
		this.drawSlot(68, 88);
		this.drawSlot(86, 88);

		/**
		 * Fortron Bar
		 */
		this.drawForce(8, 120, Math.min((float) this.tileEntity.getFortronEnergy() / (float) this.tileEntity.getFortronCapacity(), 1));
	}

}
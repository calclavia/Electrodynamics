package mffs.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import mffs.ModularForceFieldSystem;
import mffs.base.GuiMFFS;
import mffs.base.TileMFFS.TilePacketType;
import mffs.container.ContainerFortronCapacitor;
import mffs.gui.button.GuiButtonPressTransferMode;
import mffs.tile.TileFortronCapacitor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.energy.UnitDisplay.UnitPrefix;
import universalelectricity.api.vector.Vector2;

public class GuiFortronCapacitor extends GuiMFFS
{
	private TileFortronCapacitor tileEntity;

	public GuiFortronCapacitor(EntityPlayer player, TileFortronCapacitor tileentity)
	{
		super(new ContainerFortronCapacitor(player, tileentity), tileentity);
		this.tileEntity = tileentity;
	}

	@Override
	public void initGui()
	{
		this.textFieldPos = new Vector2(50, 76);
		super.initGui();
		this.buttonList.add(new GuiButtonPressTransferMode(1, this.width / 2 + 15, this.height / 2 - 37, this, this.tileEntity));

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), this.xSize / 2 - this.fontRenderer.getStringWidth(this.tileEntity.getInvName()) / 2, 6, 4210752);

		GL11.glPushMatrix();
		GL11.glRotatef(-90, 0, 0, 1);
		this.drawTextWithTooltip("upgrade", -95, 140, x, y);
		GL11.glPopMatrix();

		this.drawTextWithTooltip("linkedDevice", "%1: " + this.tileEntity.getLinkedDevices().size(), 8, 28, x, y);
		this.drawTextWithTooltip("transmissionRate", "%1: " + UnitDisplay.getDisplayShort(this.tileEntity.getTransmissionRate() * 20, Unit.LITER) + "/s", 8, 40, x, y);
		this.drawTextWithTooltip("range", "%1: " + this.tileEntity.getTransmissionRange(), 8, 52, x, y);
		this.drawTextWithTooltip("frequency", "%1:", 8, 63, x, y);
		this.textFieldFrequency.drawTextBox();
		this.drawTextWithTooltip("fortron", "%1:", 8, 95, x, y);
		this.fontRenderer.drawString(UnitDisplay.getDisplayShort(this.tileEntity.getFortronEnergy(), Unit.LITER) + "/" + UnitDisplay.getDisplay(this.tileEntity.getFortronCapacity(), Unit.LITER, UnitPrefix.MILLI), 8, 105, 4210752);

		if (tileEntity.getFortronCost() > 0)
		{
			fontRenderer.drawString("\u00a74-" + UnitDisplay.getDisplayShort(tileEntity.getFortronCost() * 20, Unit.LITER) + "/s", 118, 116, 4210752);
		}

		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);

		/**
		 * Upgrade Slots
		 */
		this.drawSlot(153, 46);
		this.drawSlot(153, 66);
		this.drawSlot(153, 86);

		/**
		 * Frequency Card Slots
		 */
		this.drawSlot(8, 73);
		this.drawSlot(26, 73);

		this.drawForce(8, 115, Math.min((float) this.tileEntity.getFortronEnergy() / (float) this.tileEntity.getFortronCapacity(), 1));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if (guibutton.id == 1)
		{
			PacketDispatcher.sendPacketToServer(ModularForceFieldSystem.PACKET_TILE.getPacket(this.tileEntity, TilePacketType.TOGGLE_MODE.ordinal()));
		}
	}
}
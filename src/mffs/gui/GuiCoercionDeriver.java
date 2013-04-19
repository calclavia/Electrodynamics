package mffs.gui;

import mffs.base.GuiBase;
import mffs.container.ContainerCoercionDeriver;
import mffs.tileentity.TileEntityCoercionDeriver;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.vector.Vector2;

public class GuiCoercionDeriver extends GuiBase
{
	private TileEntityCoercionDeriver tileEntity;

	public GuiCoercionDeriver(EntityPlayer player, TileEntityCoercionDeriver tileentity)
	{
		super(new ContainerCoercionDeriver(player, tileentity), tileentity);
		this.tileEntity = tileentity;
	}

	@Override
	public void initGui()
	{
		this.textFieldPos = new Vector2(30, 43);
		super.initGui();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), this.xSize / 2 - this.fontRenderer.getStringWidth(this.tileEntity.getInvName()) / 2, 6, 4210752);

		this.drawTextWithTooltip("frequency", "%1:", 8, 30, x, y);
		this.textFieldFrequency.drawTextBox();

		GL11.glPushMatrix();
		GL11.glRotatef(-90, 0, 0, 1);
		this.drawTextWithTooltip("upgrade", -95, 140, x, y);
		GL11.glPopMatrix();

		this.drawTextWithTooltip("progress", "%1: " + (this.tileEntity.isActive() ? "Running" : "Idle"), 8, 70, x, y);
		this.drawTextWithTooltip("fortron", "%1: " + ElectricityDisplay.getDisplayShort(this.tileEntity.getFortronEnergy(), ElectricUnit.JOULES), 8, 105, x, y);

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
		 * Frequency Card Slot
		 */
		this.drawSlot(8, 40);

		/**
		 * Matter Input
		 */
		this.drawSlot(8, 82, SlotType.BATTERY);
		this.drawSlot(8 + 20, 82);

		this.drawBar(50, 84, 1);

		/**
		 * Force Power Bar
		 */
		this.drawForce(8, 115, (float) this.tileEntity.getFortronEnergy() / (float) this.tileEntity.getFortronCapacity());
	}
}
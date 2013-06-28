package mffs.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import mffs.ModularForceFieldSystem;
import mffs.base.GuiBase;
import mffs.base.TileEntityBase.TilePacketType;
import mffs.container.ContainerForceManipulator;
import mffs.tileentity.TileEntityForceManipulator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.vector.Vector2;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.vector.Region2;

public class GuiForceManipulator extends GuiBase
{
	private TileEntityForceManipulator tileEntity;

	public GuiForceManipulator(EntityPlayer player, TileEntityForceManipulator tileEntity)
	{
		super(new ContainerForceManipulator(player, tileEntity), tileEntity);
		this.tileEntity = tileEntity;
	}

	@Override
	public void initGui()
	{
		this.textFieldPos = new Vector2(48, 91);
		super.initGui();

		this.buttonList.add(new GuiButton(1, this.width / 2 + 15, this.height / 2 - 20, 40, 20, "Reset"));

		this.tooltips.put(new Region2(new Vector2(117, 44), new Vector2(117, 44).add(18)), "Mode");

		this.tooltips.put(new Region2(new Vector2(90, 17), new Vector2(90, 17).add(18)), "Up");
		this.tooltips.put(new Region2(new Vector2(90 + 18 * 3, 17), new Vector2(90 + 18 * 3, 17).add(18)), "Up");

		this.tooltips.put(new Region2(new Vector2(90, 17 + 18 * 3), new Vector2(90, 17 + 18 * 3).add(18)), "Down");
		this.tooltips.put(new Region2(new Vector2(90 + 18 * 3, 17 + 18 * 3), new Vector2(90 + 18 * 3, 17 + 18 * 3).add(18)), "Down");

		this.tooltips.put(new Region2(new Vector2(90 + 18 * 1, 17), new Vector2(90 + 18 * 1, 17).add(18)), "Front");
		this.tooltips.put(new Region2(new Vector2(90 + 18 * 2, 17), new Vector2(90 + 18 * 2, 17).add(18)), "Front");

		this.tooltips.put(new Region2(new Vector2(90 + 18 * 1, 17 + 18 * 3), new Vector2(90 + 18 * 1, 17 + 18 * 3).add(18)), "Back");
		this.tooltips.put(new Region2(new Vector2(90 + 18 * 2, 17 + 18 * 3), new Vector2(90 + 18 * 2, 17 + 18 * 3).add(18)), "Back");

		this.tooltips.put(new Region2(new Vector2(90, 17 + 18 * 1), new Vector2(90 + 18 * 1, 17 + 18 * 1).add(18)), "Left");
		this.tooltips.put(new Region2(new Vector2(90, 17 + 18 * 2), new Vector2(90 + 18 * 1, 17 + 18 * 2).add(18)), "Left");

		this.tooltips.put(new Region2(new Vector2(90 + 18 * 3, 17 + 18 * 1), new Vector2(90 + 18 * 3, 17 + 18 * 1).add(18)), "Right");
		this.tooltips.put(new Region2(new Vector2(90 + 18 * 3, 17 + 18 * 2), new Vector2(90 + 18 * 3, 17 + 18 * 2).add(18)), "Right");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), this.xSize / 2 - this.fontRenderer.getStringWidth(this.tileEntity.getInvName()) / 2, 6, 4210752);
		this.drawTextWithTooltip("frequency", "%1:", 8, 76, x, y);
		this.textFieldFrequency.drawTextBox();

		this.drawTextWithTooltip("fortron", "%1: " + ElectricityDisplay.getDisplayShort(this.tileEntity.getFortronEnergy(), ElectricUnit.JOULES) + "/" + ElectricityDisplay.getDisplayShort(this.tileEntity.getFortronCapacity(), ElectricUnit.JOULES), 8, 110, x, y);
		this.fontRenderer.drawString("\u00a74-" + ElectricityDisplay.getDisplayShort(this.tileEntity.getFortronCost(), ElectricUnit.JOULES), 120, 121, 4210752);
		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);

		// Frequency Card Slot
		this.drawSlot(9, 88);
		this.drawSlot(9 + 18, 88);

		/**
		 * Matrix Slots
		 */

		// Mode
		this.drawSlot(117, 44, SlotType.NONE, 1f, 0.4f, 0.4f);

		// Directional Modules
		for (int xSlot = 0; xSlot < 4; xSlot++)
		{
			for (int ySlot = 0; ySlot < 4; ySlot++)
			{
				if (!(xSlot == 1 && ySlot == 1) && !(xSlot == 2 && ySlot == 2) && !(xSlot == 1 && ySlot == 2) && !(xSlot == 2 && ySlot == 1))
				{
					SlotType type = SlotType.NONE;

					if (xSlot == 0 && ySlot == 0)
					{
						type = SlotType.ARR_UP_LEFT;
					}
					else if (xSlot == 0 && ySlot == 3)
					{
						type = SlotType.ARR_DOWN_LEFT;
					}
					else if (xSlot == 3 && ySlot == 0)
					{
						type = SlotType.ARR_UP_RIGHT;
					}
					else if (xSlot == 3 && ySlot == 3)
					{
						type = SlotType.ARR_DOWN_RIGHT;
					}
					else if (ySlot == 0)
					{
						type = SlotType.ARR_UP;
					}
					else if (ySlot == 3)
					{
						type = SlotType.ARR_DOWN;
					}
					else if (xSlot == 0)
					{
						type = SlotType.ARR_LEFT;
					}
					else if (xSlot == 3)
					{
						type = SlotType.ARR_RIGHT;
					}

					this.drawSlot(90 + 18 * xSlot, 17 + 18 * ySlot, type);
				}
			}
		}

		// Upgrades
		for (int xSlot = 0; xSlot < 2; xSlot++)
		{
			for (int ySlot = 0; ySlot < 3; ySlot++)
			{
				this.drawSlot(18 + 18 * xSlot, 20 + 18 * ySlot);
			}
		}

		// Fortron Bar
		this.drawForce(8, 120, Math.min((float) this.tileEntity.getFortronEnergy() / (float) this.tileEntity.getFortronCapacity(), 1));
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		super.actionPerformed(guiButton);

		if (this.frequencyTile != null && guiButton.id == 1)
		{
			PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, (TileEntity) this.frequencyTile, TilePacketType.TOGGLE_MODE.ordinal()));
		}
	}
}
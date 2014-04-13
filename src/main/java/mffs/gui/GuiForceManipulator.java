package mffs.gui;

import mffs.ModularForceFieldSystem;
import mffs.base.GuiMFFS;
import mffs.base.TileMFFS.TilePacketType;
import mffs.container.ContainerForceManipulator;
import mffs.gui.button.GuiIcon;
import mffs.tile.TileForceManipulator;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.vector.Vector2;
import calclavia.lib.prefab.vector.Rectangle;
import calclavia.lib.utility.LanguageUtility;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiForceManipulator extends GuiMFFS
{
	private TileForceManipulator tileEntity;

	public GuiForceManipulator(EntityPlayer player, TileForceManipulator tileEntity)
	{
		super(new ContainerForceManipulator(player, tileEntity), tileEntity);
		this.tileEntity = tileEntity;
	}

	@Override
	public void initGui()
	{
		this.textFieldPos = new Vector2(111, 93);
		super.initGui();

		this.buttonList.add(new GuiIcon(1, this.width / 2 - 82, this.height / 2 - 16, new ItemStack(Item.pocketSundial)));
		this.buttonList.add(new GuiIcon(2, this.width / 2 - 82, this.height / 2 - 82, null, new ItemStack(Item.redstone), new ItemStack(Block.blockRedstone)));
		this.buttonList.add(new GuiIcon(3, this.width / 2 - 82, this.height / 2 - 60, null, new ItemStack(Block.anvil)));
		this.buttonList.add(new GuiIcon(4, this.width / 2 - 82, this.height / 2 - 38, null, new ItemStack(Item.compass)));

		this.tooltips.put(new Rectangle(new Vector2(117, 44), new Vector2(117, 44).add(18)), LanguageUtility.getLocal("gui.projector.mode"));

		this.tooltips.put(new Rectangle(new Vector2(90, 17), new Vector2(90, 17).add(18)), LanguageUtility.getLocal("gui.projector.up"));
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17), new Vector2(90 + 18 * 3, 17).add(18)), LanguageUtility.getLocal("gui.projector.up"));

		this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 3), new Vector2(90, 17 + 18 * 3).add(18)), LanguageUtility.getLocal("gui.projector.down"));
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 3), new Vector2(90 + 18 * 3, 17 + 18 * 3).add(18)), LanguageUtility.getLocal("gui.projector.down"));

		String north = LanguageUtility.getLocal("gui.projector.north");
		String south = LanguageUtility.getLocal("gui.projector.south");
		String west = LanguageUtility.getLocal("gui.projector.west");
		String east = LanguageUtility.getLocal("gui.projector.east");

		if (!this.tileEntity.isAbsolute)
		{
			north = LanguageUtility.getLocal("gui.projector.front");
			south = LanguageUtility.getLocal("gui.projector.back");
			west = LanguageUtility.getLocal("gui.projector.left");
			east = LanguageUtility.getLocal("gui.projector.right");
		}

		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 1, 17), new Vector2(90 + 18 * 1, 17).add(18)), north);
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 2, 17), new Vector2(90 + 18 * 2, 17).add(18)), north);

		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 1, 17 + 18 * 3), new Vector2(90 + 18 * 1, 17 + 18 * 3).add(18)), south);
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 2, 17 + 18 * 3), new Vector2(90 + 18 * 2, 17 + 18 * 3).add(18)), south);

		this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 1), new Vector2(90 + 18 * 1, 17 + 18 * 1).add(18)), west);
		this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 2), new Vector2(90 + 18 * 1, 17 + 18 * 2).add(18)), west);

		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 1), new Vector2(90 + 18 * 3, 17 + 18 * 1).add(18)), east);
		this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 2), new Vector2(90 + 18 * 3, 17 + 18 * 2).add(18)), east);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), this.xSize / 2 - this.fontRenderer.getStringWidth(this.tileEntity.getInvName()) / 2, 6, 4210752);
		this.fontRenderer.drawString(LanguageUtility.getLocal("gui.manipulator.anchor"), 35, 60, 4210752);

		if (this.tileEntity.anchor != null)
		{
			this.fontRenderer.drawString(this.tileEntity.anchor.intX() + ", " + this.tileEntity.anchor.intY() + ", " + this.tileEntity.anchor.intZ(), 35, 70, 4210752);
		}

		this.fontRenderer.drawString(this.tileEntity.getDirection().name(), 35, 82, 4210752);
		this.fontRenderer.drawString((this.tileEntity.clientMoveTime / 20) + "s", 35, 94, 4210752);

		this.textFieldFrequency.drawTextBox();

		drawTextWithTooltip("fortron", "\u00a74Consumption: -" + UnitDisplay.getDisplayShort(this.tileEntity.getFortronCost() * 20, Unit.LITER) + "/s", 30, 110, x, y);
		drawTextWithTooltip("fortron", "\u00a7F" +UnitDisplay.getDisplayShort(this.tileEntity.getFortronEnergy(), Unit.LITER) + "/" + UnitDisplay.getDisplayShort(this.tileEntity.getFortronCapacity(), Unit.LITER), 68, 122, x, y);
		fontRenderer.drawString(LanguageUtility.getLocal("gui.manipulator.fortron"), 8, 121, 4210752);
		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		((GuiIcon) this.buttonList.get(2)).setIndex(this.tileEntity.displayMode);
		((GuiIcon) this.buttonList.get(3)).setIndex(this.tileEntity.doAnchor ? 1 : 0);
		((GuiIcon) this.buttonList.get(4)).setIndex(this.tileEntity.isAbsolute ? 1 : 0);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);

		// Frequency Card Slot
		this.drawSlot(72, 90);
		this.drawSlot(72 + 18, 90);

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

		// General
		for (int xSlot = 0; xSlot < 3; xSlot++)
		{
			for (int ySlot = 0; ySlot < 2; ySlot++)
			{
				this.drawSlot(30 + 18 * xSlot, 18 + 18 * ySlot);
			}
		}

		// Fortron Bar
		this.drawForce(60, 120, Math.min((float) this.tileEntity.getFortronEnergy() / (float) this.tileEntity.getFortronCapacity(), 1));
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		super.actionPerformed(guiButton);

		if (guiButton.id == 1)
		{
			PacketDispatcher.sendPacketToServer(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) this.frequencyTile, TilePacketType.TOGGLE_MODE.ordinal()));
		}
		else if (guiButton.id == 2)
		{
			PacketDispatcher.sendPacketToServer(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) this.frequencyTile, TilePacketType.TOGGLE_MODE_2.ordinal()));
		}
		else if (guiButton.id == 3)
		{
			PacketDispatcher.sendPacketToServer(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) this.frequencyTile, TilePacketType.TOGGLE_MODE_3.ordinal()));
		}
		else if (guiButton.id == 4)
		{
			PacketDispatcher.sendPacketToServer(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) this.frequencyTile, TilePacketType.TOGGLE_MODE_4.ordinal()));
		}
	}

}
package resonantinduction.electrical.encoder.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import resonant.lib.gui.ContainerDummy;
import resonant.lib.gui.GuiContainerBase;
import resonantinduction.electrical.encoder.TileEncoder;

public class GuiEncoderBase extends GuiContainerBase
{
	protected InventoryPlayer player;
	protected TileEncoder tileEntity;

	public GuiEncoderBase(InventoryPlayer player, TileEncoder tileEntity, Container container)
	{
		super(container);
		this.tileEntity = tileEntity;
		this.player = player;
	}

	public GuiEncoderBase(InventoryPlayer player, TileEncoder tileEntity)
	{
		this(player, tileEntity, new ContainerDummy(tileEntity));
	}

	public int getGuiLeft()
	{
		return guiLeft;
	}

	public int getGuiTop()
	{
		return guiTop;
	}
}

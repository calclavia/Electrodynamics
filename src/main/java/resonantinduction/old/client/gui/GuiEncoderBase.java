package resonantinduction.old.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import resonantinduction.core.prefab.tile.TileEntityMachine;
import resonantinduction.mechanical.CommonProxy;
import resonantinduction.old.lib.prefab.invgui.ContainerFake;
import resonantinduction.old.lib.prefab.invgui.GuiMachineContainer;
import resonantinduction.old.transport.ResonantInductionTransport;

public class GuiEncoderBase extends GuiMachineContainer
{
	//
	public GuiEncoderBase(InventoryPlayer player, TileEntityMachine tileEntity, Container container)
	{
		super(ResonantInductionTransport.instance, container, player, tileEntity);
		this.guiID = CommonProxy.GUI_ENCODER;
		this.guiID2 = CommonProxy.GUI_ENCODER_CODE;
		this.guiID3 = CommonProxy.GUI_ENCODER_HELP;
		this.invName = "Main";
		this.invName2 = "Coder";
		this.invName3 = "Help";
	}

	public GuiEncoderBase(InventoryPlayer player, TileEntityMachine tileEntity)
	{
		this(player, tileEntity, new ContainerFake(tileEntity));
	}

}

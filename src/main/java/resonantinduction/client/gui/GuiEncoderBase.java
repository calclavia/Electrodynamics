package resonantinduction.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import resonantinduction.transport.ResonantInductionTransport;

import com.builtbroken.minecraft.prefab.TileEntityMachine;
import com.builtbroken.minecraft.prefab.invgui.ContainerFake;
import com.builtbroken.minecraft.prefab.invgui.GuiMachineContainer;

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

package resonantinduction.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import resonantinduction.transport.ResonantInductionTransport;
import resonantinduction.transport.encoder.TileEntityEncoder;

public class GuiEncoderHelp extends GuiEncoderBase
{
	public static final ResourceLocation TEXTURE_CODE_BACK = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.GUI_DIRECTORY + "gui_encoder_coder.png");

	public GuiEncoderHelp(InventoryPlayer player, TileEntityEncoder tileEntity)
	{
		super(player, tileEntity);
	}
}

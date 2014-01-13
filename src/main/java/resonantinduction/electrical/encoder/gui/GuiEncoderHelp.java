package resonantinduction.electrical.encoder.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import resonantinduction.core.Reference;
import resonantinduction.electrical.encoder.TileEncoder;

public class GuiEncoderHelp extends GuiEncoderBase
{
	public static final ResourceLocation TEXTURE_CODE_BACK = new ResourceLocation(Reference.DOMAIN, Reference.GUI_DIRECTORY + "gui_encoder_coder.png");

	public GuiEncoderHelp(InventoryPlayer player, TileEncoder tileEntity)
	{
		super(player, tileEntity);
	}
}

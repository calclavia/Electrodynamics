package resonantinduction.client.gui;

import resonantinduction.AssemblyLine;
import resonantinduction.transport.encoder.TileEntityEncoder;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiEncoderHelp extends GuiEncoderBase
{
    public static final ResourceLocation TEXTURE_CODE_BACK = new ResourceLocation(AssemblyLine.DOMAIN, AssemblyLine.GUI_DIRECTORY + "gui_encoder_coder.png");

    public GuiEncoderHelp(InventoryPlayer player, TileEntityEncoder tileEntity)
    {
        super(player, tileEntity);
    }
}

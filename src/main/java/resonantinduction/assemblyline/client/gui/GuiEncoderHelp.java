package resonantinduction.assemblyline.client.gui;

import resonantinduction.assemblyline.AssemblyLine;
import resonantinduction.assemblyline.machine.encoder.TileEntityEncoder;
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

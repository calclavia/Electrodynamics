package com.builtbroken.assemblyline.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.machine.encoder.TileEntityEncoder;

public class GuiEncoderHelp extends GuiEncoderBase
{
    public static final ResourceLocation TEXTURE_CODE_BACK = new ResourceLocation(AssemblyLine.DOMAIN, AssemblyLine.GUI_DIRECTORY + "gui_encoder_coder.png");

    public GuiEncoderHelp(InventoryPlayer player, TileEntityEncoder tileEntity)
    {
        super(player, tileEntity);
    }
}

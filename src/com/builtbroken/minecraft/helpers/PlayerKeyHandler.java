package com.builtbroken.minecraft.helpers;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.builtbroken.minecraft.network.PacketManagerKeyEvent;

import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

/** This class handles keys already binded to the game so to avoid creating new key bindings
 * 
 * @author DarkGuardsman */
public class PlayerKeyHandler implements IScheduledTickHandler
{

    @Override
    public final void tickStart(EnumSet<TickType> type, Object... tickData)
    {
        keyTick(type, false);
    }

    @Override
    public final void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        keyTick(type, true);
    }

    private void keyTick(EnumSet<TickType> type, boolean tickEnd)
    {
        for (int i = 0; i < Minecraft.getMinecraft().gameSettings.keyBindings.length; i++)
        {
            KeyBinding keyBinding = Minecraft.getMinecraft().gameSettings.keyBindings[i];
            int keyCode = keyBinding.keyCode;
            boolean state = (keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
            if (state)
            {
                PacketManagerKeyEvent.sendPacket(keyCode);
            }
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel()
    {
        return "[CoreMachine]KeyBindingCatcher";
    }

    @Override
    public int nextTickSpacing()
    {
        return 2;
    }
}

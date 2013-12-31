package com.builtbroken.minecraft.network;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ISimpleItemPacketReceiver
{
    public boolean simplePacket(EntityPlayer player, ItemStack stack, String id, ByteArrayDataInput data);
}

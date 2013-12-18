package com.dark.network;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;

public interface IPacketManager
{
    public int getID();

    public void setID(int maxID);

    public void handlePacket(INetworkManager network, Packet250CustomPayload packet, Player player, ByteArrayDataInput data);
}

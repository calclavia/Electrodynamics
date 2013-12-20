package com.builtbroken.minecraft.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.builtbroken.minecraft.DarkCore;
import com.builtbroken.minecraft.interfaces.IControlReceiver;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketManagerKeyEvent implements IPacketManager
{
    static int packetID = 0;

    private static PacketManagerKeyEvent instance;

    private List<IControlReceiver> receivers = new ArrayList<IControlReceiver>();

    public static PacketManagerKeyEvent instance()
    {
        if (instance == null)
        {
            instance = new PacketManagerKeyEvent();
        }
        return instance;
    }

    public void register(IControlReceiver rec)
    {
        if (!this.receivers.contains(rec))
        {
            this.receivers.add(rec);
        }
    }

    public void remove(IControlReceiver rec)
    {
        this.receivers.remove(rec);
    }

    @Override
    public int getID()
    {
        return packetID;
    }

    @Override
    public void setID(int maxID)
    {
        packetID = maxID;
    }

    @Override
    public void handlePacket(INetworkManager network, Packet250CustomPayload packet, Player player, ByteArrayDataInput data)
    {
        try
        {
            int key = data.readInt();
            for (IControlReceiver receiver : instance().receivers)
            {
                if (receiver.keyTyped((EntityPlayer) player, key))
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void sendPacket(int key)
    {
        PacketDispatcher.sendPacketToServer(PacketHandler.instance().getPacketWithID(DarkCore.CHANNEL, PacketManagerKeyEvent.packetID, key));
    }
}

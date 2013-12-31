package com.builtbroken.minecraft.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;

/** Helps update the server or client with general information about an item. Should mostly be used
 * for GUIs or render effects.
 * 
 * @author DarkGuardsman */
public class PacketManagerItem implements IPacketManager
{
    static int packetID = 0;

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
            EntityPlayer entityPlayer = (EntityPlayer) player;
            String id = data.readUTF();
            int slot = data.readInt();
            ItemStack stack = null;
            if (slot >= 0)
            {
                stack = entityPlayer.inventory.getStackInSlot(slot);
            }
            else if (slot == -1)
            {
                stack = entityPlayer.getHeldItem();
            }
            if (stack != null & stack.getItem() != null)
            {
                if (stack.getItem() instanceof ISimpleItemPacketReceiver)
                {
                    ((ISimpleItemPacketReceiver) stack.getItem()).simplePacket(entityPlayer, stack, id, data);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("[CoreLibrary] Error reading packet for item data");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("resource")
    public static Packet getPacket(EntityPlayer player, String channelName, String id, int slot, Object... dataToSend)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);

        try
        {
            data.writeInt(packetID);
            data.writeUTF(id);
            data.writeInt(slot);
            data = PacketHandler.instance.encodeDataStream(data, dataToSend);

            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = channelName;
            packet.data = bytes.toByteArray();
            packet.length = packet.data.length;

            return packet;
        }
        catch (IOException e)
        {
            System.out.println("Failed to create packet.");
            e.printStackTrace();
        }
        return null;
    }
}

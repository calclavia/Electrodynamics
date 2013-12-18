package com.dark.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

import com.dark.DarkCore;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketManagerEntity implements IPacketManager
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
            int entityId = data.readInt();

            World world = ((EntityPlayer) player).worldObj;
            if (world != null)
            {
                Entity entity = world.getEntityByID(entityId);
                if (entity instanceof ISimplePacketReceiver)
                {
                    String id = data.readUTF();
                    ((ISimplePacketReceiver) entity).simplePacket(id, data, player);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("[CoreMachine] Error reading packet for an entity");
            e.printStackTrace();
        }

    }

    public static void sendEntityUpdatePacket(Entity entity, boolean toServer, String id, Object... objects)
    {
        Object[] obj = new Object[2 + objects.length];
        obj[0] = entity.entityId;
        obj[1] = id;
        for (int i = 0; i < objects.length; i++)
        {
            obj[2 + i] = objects[i];
        }
        Packet packet = PacketHandler.instance().getPacketWithID(DarkCore.CHANNEL, packetID, obj);
        if (toServer)
        {
            PacketDispatcher.sendPacketToServer(packet);
        }
        else
        {
            PacketHandler.instance().sendPacketToClients(packet, entity.worldObj, new Vector3(entity), 64);
        }
    }
}

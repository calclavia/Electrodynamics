package com.builtbroken.minecraft.network;

import java.awt.Color;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.CoreRegistry;
import com.builtbroken.minecraft.DarkCore;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;

public class PacketManagerEffects implements IPacketManager
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
            World world = ((EntityPlayer) player).worldObj;
            String effectName = data.readUTF();
            if (world != null)
            {
                if (effectName.equalsIgnoreCase("laser"))
                {
                    System.out.println("Received laser packet");
                    CoreRegistry.proxy().renderBeam(world, PacketHandler.readVector3(data), PacketHandler.readVector3(data), new Color(data.readInt(), data.readInt(), data.readInt()), data.readInt());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("[CoreMachine] Error reading packet for effect rendering");
            e.printStackTrace();
        }

    }

    public static void sendClientLaserEffect(World world, Vector3 position, Vector3 target, Color color, int age)
    {
        Packet packet = PacketHandler.instance().getPacketWithID(DarkCore.CHANNEL, packetID, "laser", position, target, color.getRed(), color.getBlue(), color.getGreen(), age);
        PacketHandler.instance().sendPacketToClients(packet, world, position, position.distance(target));
        System.out.println("Sent laser render packet to client");
    }

}

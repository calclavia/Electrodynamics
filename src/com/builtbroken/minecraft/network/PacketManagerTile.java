package com.builtbroken.minecraft.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.prefab.network.IPacketReceiver;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;

public class PacketManagerTile implements IPacketManager
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
            int x = data.readInt();
            int y = data.readInt();
            int z = data.readInt();

            World world = ((EntityPlayer) player).worldObj;

            if (world != null)
            {
                TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

                if (tileEntity != null)
                {
                    PacketDataWatcher.instance.onPacketData(tileEntity, packet, System.currentTimeMillis());
                    if (tileEntity instanceof ISimplePacketReceiver)
                    {
                        String pId = data.readUTF();
                        ((ISimplePacketReceiver) tileEntity).simplePacket(pId, data, player);
                    }
                    if (tileEntity instanceof IPacketReceiver)
                    {
                        ((IPacketReceiver) tileEntity).handlePacketData(network, 0, packet, ((EntityPlayer) player), data);
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("[CoreMachine] Error reading packet at tile packet manager");
            e.printStackTrace();
        }

    }
}

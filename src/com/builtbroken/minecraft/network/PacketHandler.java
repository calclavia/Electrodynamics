package com.builtbroken.minecraft.network;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/** Packet manager based off the PacketManager from UE created by Calclavia. However changed so it
 * can easily be extended without changing the base handler file. Instead of changing the base file
 * manager can be registered to the handle that pickup on ids and then handle there own data from
 * those ids. This lets new and complex handling to be created without many overlap or long methods
 * in the packet handler
 *
 * @author DarkGuardsman */
public class PacketHandler implements IPacketHandler, IPacketReceiver
{
    protected static PacketHandler instance;

    public static HashMap<Integer, IPacketManager> packetTypes = new HashMap<Integer, IPacketManager>();

    public static PacketManagerTile tile = new PacketManagerTile();

    public static int maxID = 0;
    static
    {
        registerManager(new PacketManagerTile());
        registerManager(PacketManagerKeyEvent.instance());
        registerManager(new PacketManagerEntity());
    }

    public static void registerManager(IPacketManager manager)
    {
        if (manager != null)
        {
            packetTypes.put(maxID, manager);
            manager.setID(maxID);
            maxID++;

        }
    }

    public static IPacketManager getManager(int id)
    {
        return packetTypes.get(id);
    }

    public static PacketHandler instance()
    {
        if (instance == null)
        {
            instance = new PacketHandler();
        }
        return instance;
    }

    /** Writes a compressed NBTTagCompound to the OutputStream */
    public void writeNBTTagCompound(NBTTagCompound tag, DataOutputStream dataStream) throws IOException
    {
        if (tag == null)
        {
            dataStream.writeShort(-1);
        }
        else
        {
            byte[] var2 = CompressedStreamTools.compress(tag);
            dataStream.writeShort((short) var2.length);
            dataStream.write(var2);
        }
    }

    public void writeNBTTagCompound(NBTTagCompound tag, ByteArrayDataOutput dataStream) throws IOException
    {
        if (tag == null)
        {
            dataStream.writeShort(-1);
        }
        else
        {
            byte[] var2 = CompressedStreamTools.compress(tag);
            dataStream.writeShort((short) var2.length);
            dataStream.write(var2);
        }
    }

    /** Reads a compressed NBTTagCompount in a ByteStream. */
    public NBTTagCompound readNBTTagCompound(DataInputStream dataStream) throws IOException
    {
        short var1 = dataStream.readShort();

        if (var1 < 0)
        {
            return null;
        }
        else
        {
            byte[] var2 = new byte[var1];
            dataStream.readFully(var2);
            return CompressedStreamTools.decompress(var2);
        }
    }

    public NBTTagCompound readNBTTagCompound(ByteArrayDataInput dataStream) throws IOException
    {
        short var1 = dataStream.readShort();

        if (var1 < 0)
        {
            return null;
        }
        else
        {
            byte[] var2 = new byte[var1];
            dataStream.readFully(var2);
            return CompressedStreamTools.decompress(var2);
        }
    }

    public static Vector3 readVector3(ByteArrayDataInput data) throws IOException
    {
        return new Vector3(data.readDouble(), data.readDouble(), data.readDouble());
    }

    @SuppressWarnings("resource")
    public Packet getPacketWithID(String channelName, int id, Object... sendData)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);

        try
        {
            data.writeInt(id);
            data = encodeDataStream(data, sendData);

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

    public Packet getPacket(String channelName, Object... sendData)
    {
        return getPacketWithID(channelName, -1, sendData);
    }

    /** Gets a packet for the tile entity.
     *
     * @return */
    @SuppressWarnings("resource")
    public Packet getTilePacket(String channelName, TileEntity sender, Object... sendData)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);

        try
        {
            data.writeInt(PacketHandler.tile.getID());

            data.writeInt(sender.xCoord);
            data.writeInt(sender.yCoord);
            data.writeInt(sender.zCoord);
            data = encodeDataStream(data, sendData);

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

    /** Sends packets to clients around a specific coordinate. A wrapper using Vector3. See
     * {@PacketDispatcher} for detailed information. */
    public void sendPacketToClients(Packet packet, World worldObj, Vector3 position, double range)
    {
        try
        {
            PacketDispatcher.sendPacketToAllAround(position.x, position.y, position.z, range, worldObj.provider.dimensionId, packet);
        }
        catch (Exception e)
        {
            System.out.println("Sending packet to client failed.");
            e.printStackTrace();
        }
    }

    /** Sends a packet to all the clients on this server. */
    public void sendPacketToClients(Packet packet, World worldObj)
    {
        try
        {
            PacketDispatcher.sendPacketToAllInDimension(packet, worldObj.provider.dimensionId);
        }
        catch (Exception e)
        {
            System.out.println("Sending packet to client failed.");
            e.printStackTrace();
        }
    }

    public void sendPacketToClients(Packet packet)
    {
        try
        {
            PacketDispatcher.sendPacketToAllPlayers(packet);
        }
        catch (Exception e)
        {
            System.out.println("Sending packet to client failed.");
            e.printStackTrace();
        }
    }

    public DataOutputStream encodeDataStream(DataOutputStream data, Object... sendData)
    {
        try
        {
            for (Object dataValue : sendData)
            {
                if (dataValue instanceof Vector3)
                {
                    data.writeDouble(((Vector3) dataValue).x);
                    data.writeDouble(((Vector3) dataValue).y);
                    data.writeDouble(((Vector3) dataValue).z);
                }
                else if (dataValue instanceof Integer)
                {
                    data.writeInt((Integer) dataValue);
                }
                else if (dataValue instanceof Float)
                {
                    data.writeFloat((Float) dataValue);
                }
                else if (dataValue instanceof Double)
                {
                    data.writeDouble((Double) dataValue);
                }
                else if (dataValue instanceof Byte)
                {
                    data.writeByte((Byte) dataValue);
                }
                else if (dataValue instanceof Boolean)
                {
                    data.writeBoolean((Boolean) dataValue);
                }
                else if (dataValue instanceof String)
                {
                    data.writeUTF((String) dataValue);
                }
                else if (dataValue instanceof Short)
                {
                    data.writeShort((Short) dataValue);
                }
                else if (dataValue instanceof Long)
                {
                    data.writeLong((Long) dataValue);
                }
                else if (dataValue instanceof NBTTagCompound)
                {
                    writeNBTTagCompound((NBTTagCompound) dataValue, data);
                }
            }

            return data;
        }
        catch (IOException e)
        {
            System.out.println("Packet data encoding failed.");
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
    {
        try
        {
            ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);

            int packetTypeID = data.readInt();

            IPacketManager packetType = getManager(packetTypeID);

            if (packetType != null)
            {
                packetType.handlePacket(network, packet, player, data);
            }
            else
            {
                this.handlePacketData(network, packetTypeID, packet, ((EntityPlayer) player), data);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
    {

    }
}
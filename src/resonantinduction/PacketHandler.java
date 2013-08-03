/**
 * 
 */
package resonantinduction;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.base.IPacketReceiver;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * @author AidanBrady
 * 
 */
public class PacketHandler implements IPacketHandler
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		if (packet.channel == ResonantInduction.CHANNEL)
		{
			ByteArrayDataInput dataStream = ByteStreams.newDataInput(packet.data);
			EntityPlayer entityplayer = (EntityPlayer) player;
			World world = entityplayer.worldObj;

			try
			{
				int packetType = dataStream.readInt();

				if (packetType == PacketType.TILE.ordinal())
				{
					int x = dataStream.readInt();
					int y = dataStream.readInt();
					int z = dataStream.readInt();

					TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

					if (tileEntity instanceof IPacketReceiver)
					{
						((IPacketReceiver) tileEntity).handle(dataStream);
					}
				}
				else if (packetType == PacketType.DATA_REQUEST.ordinal())
				{
					int x = dataStream.readInt();
					int y = dataStream.readInt();
					int z = dataStream.readInt();
					
					TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
					
					if (tileEntity instanceof IPacketReceiver)
					{
						sendTileEntityPacketToClients(tileEntity, ((IPacketReceiver) tileEntity).getNetworkedData(new ArrayList()).toArray());
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void encode(Object[] dataValues, DataOutputStream output)
	{
		try
		{
			for (Object data : dataValues)
			{
				if (data instanceof Integer)
				{
					output.writeInt((Integer) data);
				}
				else if (data instanceof Boolean)
				{
					output.writeBoolean((Boolean) data);
				}
				else if (data instanceof Double)
				{
					output.writeDouble((Double) data);
				}
				else if (data instanceof Float)
				{
					output.writeFloat((Float) data);
				}
				else if (data instanceof String)
				{
					output.writeUTF((String) data);
				}
				else if (data instanceof Byte)
				{
					output.writeByte((Byte) data);
				}
				else if (data instanceof Object[])
				{
					encode((Object[])data, output);
				}
			}
		}
		catch (Exception e)
		{
		}
	}
	
	public static void sendDataRequest(TileEntity tileEntity)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try
		{
			data.writeInt(PacketType.DATA_REQUEST.ordinal());
			data.writeInt(tileEntity.xCoord);
			data.writeInt(tileEntity.yCoord);
			data.writeInt(tileEntity.zCoord);
		}
		catch (Exception e)
		{
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = ResonantInduction.CHANNEL;
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;
		
		PacketDispatcher.sendPacketToServer(packet);
	}

	public static void sendTileEntityPacketToServer(TileEntity tileEntity, Object... dataValues)
	{
		PacketDispatcher.sendPacketToServer(getTileEntityPacket(tileEntity, dataValues));
	}

	public static void sendTileEntityPacketToClients(TileEntity tileEntity, Object... dataValues)
	{
		PacketDispatcher.sendPacketToAllPlayers(getTileEntityPacket(tileEntity, dataValues));
	}

	public static Packet250CustomPayload getTileEntityPacket(TileEntity tileEntity, Object... dataValues)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try
		{
			data.writeInt(PacketType.TILE.ordinal());
			data.writeInt(tileEntity.xCoord);
			data.writeInt(tileEntity.yCoord);
			data.writeInt(tileEntity.zCoord);

			encode(dataValues, data);
		}
		catch (Exception e)
		{
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = ResonantInduction.CHANNEL;
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;

		return packet;
	}

	public static enum PacketType
	{
		TILE,
		DATA_REQUEST
	}
}

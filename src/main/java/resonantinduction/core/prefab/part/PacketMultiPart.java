package resonantinduction.core.prefab.part;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketType;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.google.common.io.ByteArrayDataInput;

/**
 * Packet handler for blocks and tile entities.
 * 
 * @author Calclavia
 */
public class PacketMultiPart extends PacketType
{
	public PacketMultiPart(String channel)
	{
		super(channel);
	}

	public Packet getPacket(Vector3 position, int partID, Object... args)
	{
		List newArgs = new ArrayList();

		newArgs.add(position.intX());
		newArgs.add(position.intY());
		newArgs.add(position.intZ());
		newArgs.add(partID);

		for (Object obj : args)
		{
			newArgs.add(obj);
		}

		return super.getPacket(newArgs.toArray());
	}

	@Override
	public void receivePacket(ByteArrayDataInput data, EntityPlayer player)
	{
		int x = data.readInt();
		int y = data.readInt();
		int z = data.readInt();
		TileEntity tileEntity = player.worldObj.getBlockTileEntity(x, y, z);
		
		if (tileEntity instanceof TileMultipart)
		{
			TMultiPart part = ((TileMultipart) tileEntity).partMap(data.readInt());

			if (part instanceof IPacketReceiver)
			{
				((IPacketReceiver) part).onReceivePacket(data, player);
			}
		}
	}
}

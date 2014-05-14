package resonantinduction.mechanical.energy.grid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketType;
import universalelectricity.api.net.IConnector;
import universalelectricity.api.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

/**
 * Packet handler for a grid network.
 * 
 * @author Calclavia
 */
public class PacketNetwork<C extends IConnector> extends PacketType
{
	private Class connectorClass;

	public PacketNetwork(Class networkClass, String channel)
	{
		super(channel);
		this.connectorClass = networkClass;
	}

	public Packet getPacket(int x, int y, int z, int dir, Object... args)
	{
		List newArgs = new ArrayList();

		newArgs.add(x);
		newArgs.add(y);
		newArgs.add(z);
		newArgs.add(dir);

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
		System.out.println(tileEntity + " rec: " + new Vector3(x, y, z));
		// TODO: Somehow this would receive the wrong coordinate once in a while...
		if (tileEntity != null && connectorClass.isAssignableFrom(tileEntity.getClass()))
		{
			C instance = (C) ((C) tileEntity).getInstance(ForgeDirection.getOrientation(data.readInt()));
			Object network = instance.getNetwork();

			if (network instanceof IPacketReceiver)
			{
				((IPacketReceiver) network).onReceivePacket(data, player, instance);
			}
		}
	}
}

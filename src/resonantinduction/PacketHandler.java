/**
 * 
 */
package resonantinduction;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

/**
 * @author Calclavia
 * 
 */
public class PacketHandler implements IPacketHandler
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cpw.mods.fml.common.network.IPacketHandler#onPacketData(net.minecraft.network.INetworkManager
	 * , net.minecraft.network.packet.Packet250CustomPayload, cpw.mods.fml.common.network.Player)
	 */
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{

	}

}

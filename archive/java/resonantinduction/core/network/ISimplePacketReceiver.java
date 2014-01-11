package resonantinduction.core.network;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;

/**
 * Simplified version of IPackerReceiver for tiles that only need a packet ID, data, and player
 * Reference
 * 
 * @author DarkGuardsman
 */
public interface ISimplePacketReceiver
{
	/**
	 * Simplified version of IPacketReceiver's HandlePacketData
	 * 
	 * @param id - packet ID as a string
	 * @param data - data from the packet, after location has been read
	 * @param player - player that the packet was sent to or came from
	 * @return true if the packet was used
	 */
	public boolean simplePacket(String id, ByteArrayDataInput data, Player player);

}
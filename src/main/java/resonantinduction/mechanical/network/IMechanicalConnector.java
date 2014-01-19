package resonantinduction.mechanical.network;

import universalelectricity.api.net.IConnector;

/**
 * Applied to connectors in a mechanical network
 * 
 * @author Calclavia
 */
public interface IMechanicalConnector extends IMechanical, IConnector<IMechanicalNetwork>
{
	/**
	 * Uses this connector to send a packet to the client for the network.
	 * 
	 * @return True if the packet was successfully sent.
	 */
	public boolean sendNetworkPacket(long torque, float angularVelocity);

	/**
	 * The percentage of resistance caused by this connector.
	 * 
	 * @return A small value, most likely less than one.
	 */
	public float getResistance();
}

package resonantinduction.mechanical.network;

import universalelectricity.api.net.IConnectable;
import universalelectricity.api.net.IConnector;

/**
 * Applied to connectors in a mechanical network
 * 
 * @author Calclavia
 */
public interface IMechanicalConnector extends IMechanical, IConnector<IMechanicalNetwork>
{
	/**
	 * An update called by the network.
	 */
	public void onNetworkChanged();

	/**
	 * Uses this connector to send a packet to the client.
	 */
	public void sendNetworkPacket(long torque, float angularVelocity);
}

package resonantinduction.mechanical.network;

import universalelectricity.api.net.IConnector;

/**
 * For the mechanical network.
 * 
 * @author Calclavia
 * 
 */
public interface IMechConnector extends IConnector<IMechNetwork>
{
	public long getTorque();

	public void setTorque(long torque);
}

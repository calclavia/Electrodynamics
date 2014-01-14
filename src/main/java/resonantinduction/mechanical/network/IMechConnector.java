package resonantinduction.mechanical.network;

import universalelectricity.api.net.IConnector;

/** For the mechanical network.
 * 
 * @author Calclavia */
public interface IMechConnector extends IConnector<IMechNetwork>
{
    public int getResistance();
}

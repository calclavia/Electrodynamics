package resonantinduction.mechanical.network;

import universalelectricity.api.net.IConnector;

/** For the mechanical network.
 * 
 * @author Calclavia */
public interface IMechanicalConnector extends IConnector<IMechanicalNetwork>
{
    public int getResistance();
}

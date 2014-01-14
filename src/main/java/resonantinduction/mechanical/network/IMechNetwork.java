package resonantinduction.mechanical.network;

import universalelectricity.api.net.INetwork;

/** Mechanical network in interface form for interaction or extension
 * 
 * @author DarkGuardsman */
public interface IMechNetwork extends INetwork<IMechNetwork, IMechConnector, IMechMachine>
{
    public int getForce();

    public int getRotSpeed();

    public int getTorque();
}

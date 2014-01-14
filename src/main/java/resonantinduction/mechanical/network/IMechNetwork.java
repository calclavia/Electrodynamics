package resonantinduction.mechanical.network;

import universalelectricity.api.net.INetwork;

/** Mechanical network in interface form for interaction or extension
 * 
 * @author DarkGuardsman */
public interface IMechNetwork extends INetwork<IMechNetwork, IMechConnector, IMechMachine>
{
    /** Power applied by the network at the given speed */
    public int getTorque();

    /** Rotation of the the network in a single update */
    public int getRotationPerTick();

    /** Called to rebuild the network */
    public void reconstruct();
}

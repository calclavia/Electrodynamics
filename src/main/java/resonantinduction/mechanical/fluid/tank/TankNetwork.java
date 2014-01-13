package resonantinduction.mechanical.fluid.tank;

import resonantinduction.mechanical.fluid.network.FluidNetwork;

/** Network that handles connected tanks
 * 
 * @author DarkGuardsman */
public class TankNetwork extends FluidNetwork
{
    public TankNetwork()
    {

    }

    public TankNetwork(TileTank... tanks)
    {
        super(tanks);
    }
}

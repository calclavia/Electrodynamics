package resonantinduction.mechanical.network;

import universalelectricity.core.net.Network;

/**
 * @author Calclavia
 * 
 */
public class MechNetwork extends Network<IMechNetwork, IMechConnector, IMechMachine> implements IMechNetwork
{
	private long energyBuffer;

	@Override
	public void update()
	{
		
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean continueUpdate()
	{
		return true;
	}
	
	public void addTorque(long energy)
	{
		this.energyBuffer += energy;
	}

	@Override
	public void split(IMechConnector connection)
	{
		
	}

	@Override
	public void split(IMechConnector connectorA, IMechConnector connectorB)
	{
		
	}

    @Override
    public IMechNetwork merge(IMechNetwork network)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getForce()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRotSpeed()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getTorque()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}

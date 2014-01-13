package resonantinduction.mechanical.network;

import universalelectricity.core.net.Network;

/**
 * @author Calclavia
 * 
 */
public class MechanicalNetwork extends Network<MechanicalNetwork, IMechanical, IMechanical>
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
	public MechanicalNetwork merge(MechanicalNetwork network)
	{
		return null;
	}

	@Override
	public void split(IMechanical connection)
	{
		
	}

	@Override
	public void split(IMechanical connectorA, IMechanical connectorB)
	{
		
	}

}

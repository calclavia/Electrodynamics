package resonantinduction.transport;

import resonatninduction.tilenetwork.INetworkPart;
import resonatninduction.tilenetwork.prefab.NetworkSharedPower;
import resonatninduction.tilenetwork.prefab.NetworkUpdateHandler;

public class NetworkAssembly extends NetworkSharedPower
{
	private long networkPartEnergyRequest = 0;
	private long lastUpdateTime = 0;
	static
	{
		NetworkUpdateHandler.registerNetworkClass("AssemblyNet", NetworkAssembly.class);
	}

	public NetworkAssembly()
	{
		super();
	}

	public NetworkAssembly(INetworkPart... parts)
	{
		super(parts);
	}

	/** Gets the demand of all parts of the network including network parts */
	public float getNetworkDemand()
	{
		if (System.currentTimeMillis() - this.lastUpdateTime > 100)
		{
			this.networkPartEnergyRequest = 0;
			this.lastUpdateTime = System.currentTimeMillis();
			for (INetworkPart part : this.getMembers())
			{
				if (part instanceof TileEntityAssembly)
				{
					networkPartEnergyRequest += ((TileEntityAssembly) part).getWattLoad();
					networkPartEnergyRequest += ((TileEntityAssembly) part).getExtraLoad();
				}
			}
		}
		return networkPartEnergyRequest;
	}

	@Override
	public boolean isValidMember(INetworkPart part)
	{
		return super.isValidMember(part) && part instanceof TileEntityAssembly;
	}

}

package resonantinduction.multimeter;

import java.util.HashMap;

import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import universalelectricity.api.electricity.ElectricalEvent.EnergyUpdateEvent;
import universalelectricity.core.grid.IEnergyNetwork;

/**
 * @author Calclavia
 * 
 */
public class MultimeterEventHandler
{
	private static final HashMap<IEnergyNetwork, Integer> networkEnergyCache = new HashMap<IEnergyNetwork, Integer>();
	private static long lastCheckTime = 0;

	public static HashMap<IEnergyNetwork, Float> getCache(World worldObj)
	{
		HashMap<IEnergyNetwork, Float> returnCache = (HashMap<IEnergyNetwork, Float>) networkEnergyCache.clone();

		if (Math.abs(worldObj.getWorldTime() - lastCheckTime) >= 40)
		{
			lastCheckTime = worldObj.getWorldTime();
			networkEnergyCache.clear();
		}

		return returnCache;
	}

	@ForgeSubscribe
	public void event(EnergyUpdateEvent evt)
	{
		IEnergyNetwork network = evt.network;

		if (network.getDistribution(null) != 0)
		{
			networkEnergyCache.put(network, network.getDistribution(null));
		}
	}
}

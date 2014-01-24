package resonantinduction.mechanical.process;

import java.util.HashMap;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.item.EntityItem;

public class Timer<K>
{
	private final HashMap<K, Integer> clientTimer = new HashMap<K, Integer>();
	private final HashMap<K, Integer> serverTimer = new HashMap<K, Integer>();

	public void put(K key, int defaultTime)
	{
		getTimeMap().put(key, defaultTime);
	}

	public boolean containsKey(K key)
	{
		return getTimeMap().containsKey(key);
	}

	public void remove(K key)
	{
		getTimeMap().remove(key);
	}

	public int decrease(K key)
	{
		return decrease(key, 1);
	}

	public int decrease(K key, int amount)
	{
		int timeLeft = getTimeMap().get(key) - amount;
		getTimeMap().put(key, timeLeft);
		return timeLeft;
	}

	public HashMap<K, Integer> getTimeMap()
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			return serverTimer;
		}

		return clientTimer;
	}

}

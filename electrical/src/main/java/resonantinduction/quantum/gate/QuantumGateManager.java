package resonantinduction.quantum.gate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

public class QuantumGateManager
{
	private static HashMap<Integer, QuantumGateManager> managerList = new HashMap<Integer, QuantumGateManager>();
	private HashSet<TileQuantumGate> teleporters = new HashSet<TileQuantumGate>();
	private static HashMap<String, Long> coolDown = new HashMap<String, Long>();

	public static QuantumGateManager getManagerForDim(int dim)
	{
		if (managerList.get(dim) == null)
		{
			managerList.put(dim, new QuantumGateManager());
		}
		
		return managerList.get(dim);
	}

	/** Adds a teleport anchor to this list or anchors */
	public static void addAnchor(TileQuantumGate anch)
	{
		if (anch != null)
		{
			QuantumGateManager manager = getManagerForDim(anch.worldObj.provider.dimensionId);
			
			if (!manager.teleporters.contains(anch))
			{
				manager.teleporters.add(anch);
			}
		}
	}

	/** Removes a teleport anchor to this list or anchors */
	public static void remAnchor(TileQuantumGate anch)
	{
		if (anch != null)
		{
			QuantumGateManager manager = getManagerForDim(anch.worldObj.provider.dimensionId);
			manager.teleporters.remove(anch);
		}
	}

	public static HashSet<TileQuantumGate> getConnectedAnchors(World world)
	{
		return getManagerForDim(world.provider.dimensionId).teleporters;
	}

	public boolean contains(TileQuantumGate anch)
	{
		return teleporters.contains(anch);
	}

	public static TileQuantumGate getClosestWithFrequency(VectorWorld vec, int frequency, TileQuantumGate... anchors)
	{
		TileQuantumGate tele = null;
		List<TileQuantumGate> ignore = new ArrayList<TileQuantumGate>();
		if (anchors != null)
		{
			ignore.addAll(Arrays.asList(anchors));
		}
		Iterator<TileQuantumGate> it = new ArrayList(QuantumGateManager.getConnectedAnchors(vec.world)).iterator();
		while (it.hasNext())
		{
			TileQuantumGate teleporter = it.next();
			if (!ignore.contains(teleporter) && teleporter.getFrequency() == frequency)
			{
				if (tele == null || new Vector3(tele).distance(vec) > new Vector3(teleporter).distance(vec))
				{
					tele = teleporter;
				}
			}
		}
		return tele;
	}

	protected static void moveEntity(Entity entity, VectorWorld location)
	{
		if (entity != null && location != null)
		{
			location.world.markBlockForUpdate((int) location.x, (int) location.y, (int) location.z);
			if (entity instanceof EntityPlayerMP)
			{
				if (coolDown.get(((EntityPlayerMP) entity).username) == null || (System.currentTimeMillis() - coolDown.get(((EntityPlayerMP) entity).username) > 30))
				{
					((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(location.x, location.y, location.z, 0, 0);
					coolDown.put(((EntityPlayerMP) entity).username, System.currentTimeMillis());
				}
			}
			else
			{
				entity.setPosition(location.x, location.y, location.z);
			}
		}
	}
}

package resonantinduction.core.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.Pair;

/** @author DarkGuardsman */
public class BlockMapUtil
{
	public static List<Pair<Integer, Integer>> getBlocksInArea(World world, Vector3 start, Vector3 end)
	{
		Vector3 delta = end.translate(start.invert());

		return null;
	}

	public static HashMap<Vector3, Pair<Integer, Integer>> getBlocksInGrid(World world, Vector3 center, Vector3 size, Pair<Integer, Integer>... pairs)
	{
		int startX = (int) (center.x - (size.x / 2));
		int startY = (int) (center.y - (size.y / 2));
		int startZ = (int) (center.z - (size.z / 2));
		HashMap<Vector3, Pair<Integer, Integer>> mapping = new HashMap<Vector3, Pair<Integer, Integer>>();
		List<Pair<Integer, Integer>> filter = new ArrayList<Pair<Integer, Integer>>();
		if (pairs != null)
		{
			for (int i = 0; i < pairs.length; i++)
			{
				filter.add(pairs[i]);
			}
		}
		for (int y = startY; y >= startY && y < center.y + (size.y / 2) - 1; y++)
		{
			for (int x = startX; y >= startX && x < center.x + (size.x / 2) - 1; x++)
			{
				for (int z = startZ; z >= startZ && z < center.z + (size.z / 2) - 1; z++)
				{
					Pair<Integer, Integer> block = new Pair<Integer, Integer>(world.getBlockId(x, y, z), world.getBlockMetadata(x, y, z));
					if (pairs == null || filter.contains(block))
					{

						mapping.put(new Vector3(x, y, z), block);
					}
				}
			}
		}

		return mapping;
	}
}

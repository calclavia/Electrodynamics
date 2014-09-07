package resonantinduction.atomic.schematic;

import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.schematic.Schematic;
import resonant.lib.type.Pair;
import resonantinduction.atomic.AtomicContent;
import universalelectricity.core.transform.vector.Vector3;

import java.util.HashMap;

public class SchematicFusionReactor extends Schematic
{
	@Override
	public String getName()
	{
		return "schematic.fusionReactor.name";
	}

	@Override
	public HashMap<Vector3, Pair<Block, Integer>> getStructure(ForgeDirection dir, int size)
	{
		HashMap<Vector3, Pair<Block, Integer>> returnMap = new HashMap<Vector3, Pair<Block, Integer>>();

		/** Fusion Torus */
		int radius = size + 2;

		for (int x = -radius; x <= radius; x++)
		{
			for (int z = -radius; z <= radius; z++)
			{
				for (int y = 0; y <= size; y++)
				{
					Vector3 position = new Vector3(x, y, z);
					double magnitude = Math.sqrt(x * x + z * z);

					if (!returnMap.containsKey(position))
					{
						returnMap.put(position, new Pair(0, 0));
					}

					if (magnitude <= radius)
					{
						if (y == 0 || y == size)
						{
							if (magnitude >= 1)
							{
								double yDeviation = (y == 0 ? size / 3 : -size / 3) + (y == 0 ? -1 : 1) * Math.sin(magnitude / radius * Math.PI) * size / 2d;
								Vector3 newPos = position.clone().add(0, yDeviation, 0);
								returnMap.put(newPos.round(), new Pair(AtomicContent.blockElectromagnet(), 1));
							}
						}
						else if (magnitude > radius - 1)
						{
							returnMap.put(position, new Pair(AtomicContent.blockElectromagnet(), 0));
						}
					}
				}
			}
		}
		/** Fusion Core */
		for (int y = 0; y < size; y++)
		{
			returnMap.put(new Vector3(0, y, 0), new Pair(AtomicContent.blockReactorCell(), 0));
			returnMap.put(new Vector3(1, y, 0), new Pair(AtomicContent.blockElectromagnet(), 0));
			returnMap.put(new Vector3(0, y, 1), new Pair(AtomicContent.blockElectromagnet(), 0));
			returnMap.put(new Vector3(0, y, -1), new Pair(AtomicContent.blockElectromagnet(), 0));
			returnMap.put(new Vector3(-1, y, 0), new Pair(AtomicContent.blockElectromagnet(), 0));
		}

		returnMap.put(new Vector3(0, 0, 0), new Pair(AtomicContent.blockReactorCell(), 0));

		return returnMap;
	}
}

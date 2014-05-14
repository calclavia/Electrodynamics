package resonantinduction.mechanical.energy.turbine;

import java.util.HashMap;

import net.minecraftforge.common.ForgeDirection;
import resonant.lib.schematic.Schematic;
import resonant.lib.type.Pair;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.vector.Vector3;

public class SchematicWaterTurbine extends Schematic
{
	@Override
	public String getName()
	{
		return "schematic.waterTurbine.name";
	}

	@Override
	public HashMap<Vector3, Pair<Integer, Integer>> getStructure(ForgeDirection dir, int size)
	{
		HashMap<Vector3, Pair<Integer, Integer>> returnMap = new HashMap<Vector3, Pair<Integer, Integer>>();

		int r = size;

		for (int x = -r; x <= r; x++)
		{
			for (int y = -r; y <= r; y++)
			{
				for (int z = -r; z <= r; z++)
				{
					if ((dir.offsetX != 0 && x == 0) || (dir.offsetY != 0 && y == 0) || (dir.offsetZ != 0 && z == 0))
					{
						Vector3 targetPosition = new Vector3(x, y, z);
						returnMap.put(targetPosition, new Pair(Mechanical.blockWaterTurbine.blockID, dir.ordinal()));
					}
				}
			}
		}

		return returnMap;
	}
}

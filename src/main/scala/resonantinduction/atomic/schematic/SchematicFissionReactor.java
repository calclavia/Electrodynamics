package resonantinduction.atomic.schematic;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.schematic.Schematic;
import resonant.lib.type.Pair;
import resonantinduction.atomic.AtomicContent;
import universalelectricity.core.transform.vector.Vector3;

import java.util.HashMap;

public class SchematicFissionReactor extends Schematic
{
	@Override
	public String getName()
	{
		return "schematic.fissionReactor.name";
	}

	@Override
	public HashMap<Vector3, Pair<Block, Integer>> getStructure(ForgeDirection dir, int size)
	{
		HashMap<Vector3, Pair<Block, Integer>> returnMap = new HashMap<Vector3, Pair<Block, Integer>>();

		if (size <= 1)
		{
			int r = 2;

			for (int x = -r; x <= r; x++)
			{
				for (int z = -r; z <= r; z++)
				{
					Vector3 targetPosition = new Vector3(x, 0, z);
					returnMap.put(targetPosition, new Pair(Blocks.water, 0));
				}
			}

			r -= 1;

			/** Create turbines and control rods */
			for (int x = -r; x <= r; x++)
			{
				for (int z = -r; z <= r; z++)
				{
					Vector3 targetPosition = new Vector3(x, 1, z);
					returnMap.put(targetPosition, new Pair(Block.getBlockFromName("electricTurbine"), 0));

					if (!((x == -r || x == r) && (z == -r || z == r)) && new Vector3(x, 0, z).magnitude() <= 1)
					{
						returnMap.put(new Vector3(x, -1, z), new Pair(AtomicContent.blockControlRod(), 0));
						returnMap.put(new Vector3(x, -2, z), new Pair(Blocks.sticky_piston, 1));
					}
				}
			}

			returnMap.put(new Vector3(0, -1, 0), new Pair(AtomicContent.blockThermometer(), 0));
			// TODO: IF Siren is a Tile, don't do this. Redstone can't hold it.
			returnMap.put(new Vector3(0, -3, 0), new Pair(AtomicContent.blockSiren(), 0));
			returnMap.put(new Vector3(0, -2, 0), new Pair(Blocks.redstone_wire, 0));
			returnMap.put(new Vector3(), new Pair(AtomicContent.blockReactorCell(), 0));
		}
		else
		{
			int r = 2;

			for (int y = 0; y < size; y++)
			{
				for (int x = -r; x <= r; x++)
				{
					for (int z = -r; z <= r; z++)
					{
						Vector3 targetPosition = new Vector3(x, y, z);
						Vector3 leveledPosition = new Vector3(0, y, 0);

						if (y < size - 1)
						{
							if (targetPosition.distance(leveledPosition) == 2)
							{
								returnMap.put(targetPosition, new Pair(AtomicContent.blockControlRod(), 0));

								/** Place piston base to push control rods in. */
								int rotationMetadata = 0;
								Vector3 offset = new Vector3(x, 0, z).normalize();

								for (ForgeDirection checkDir : ForgeDirection.VALID_DIRECTIONS)
								{
									if (offset.x() == checkDir.offsetX && offset.y() == checkDir.offsetY && offset.z() == checkDir.offsetZ)
									{
										rotationMetadata = checkDir.getOpposite().ordinal();
										break;
									}
								}

								returnMap.put(targetPosition.clone().add(offset), new Pair(Blocks.sticky_piston, rotationMetadata));
							}
							else if (x == -r || x == r || z == -r || z == r)
							{
								returnMap.put(targetPosition, new Pair(Blocks.glass, 0));

							}
							else if (x == 0 && z == 0)
							{
								returnMap.put(targetPosition, new Pair(AtomicContent.blockReactorCell(), 0));
							}
							else
							{
								returnMap.put(targetPosition, new Pair(Blocks.water, 0));
							}
						}
						else if (targetPosition.distance(leveledPosition) < 2)
						{
							returnMap.put(targetPosition, new Pair(Block.getBlockFromName("electricTurbine"), 0));
						}
					}
				}
			}
		}

		return returnMap;
	}
}

package resonantinduction.atomic.schematic

import net.minecraft.block.Block
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.schematic.Schematic
import resonant.lib.`type`.Pair
import resonantinduction.atomic.AtomicContent
import universalelectricity.core.transform.vector.Vector3
import java.util.HashMap

class SchematicAccelerator extends Schematic
{
    def getName: String =
    {
        return "schematic.accelerator.name"
    }

    def getStructure(dir: ForgeDirection, size: Int): HashMap[Vector3, Pair[Block, Integer]] =
    {
        val returnMap: HashMap[Vector3, Pair[Block, Integer]] = new HashMap[Vector3, Pair[Block, Integer]]
        var r: Int = size

        for (x <- -r to r)
		{
			for (z  <- -r to r)
			{
				for (y  <- -r to r)
				{
					if (x == -r || x == r - 1 || z == -r || z == r - 1)
					{
						returnMap.put(new Vector3(x, y, z), new Pair(AtomicContent.blockElectromagnet, 0));
					}
				}
			}
		}

		r = size - 2;

		for (x  <- -r to r)
		{
			for (z  <- -r to r)
			{
				for (y  <- -r to r)
				{
					if (x == -r || x == r - 1 || z == -r || z == r - 1)
					{
						returnMap.put(new Vector3(x, y, z), new Pair(AtomicContent.blockElectromagnet, 0));
					}
				}
			}
		}

		r = size - 1;

		for (x  <- -r to r)
		{
			for (z  <- -r to r)
			{
				for (y  <- -r to r)
				{
					if (x == -r || x == r - 1 || z == -r || z == r - 1)
					{
						if (y == -1 || y == 1)
						{
							returnMap.put(new Vector3(x, y, z), new Pair(AtomicContent.blockElectromagnet, 1));
						}
						else if (y == 0)
						{
							returnMap.put(new Vector3(x, y, z), new Pair[Block, Integer](null, 0));
						}
					}
				}
			}
		}
        return returnMap
    }
}
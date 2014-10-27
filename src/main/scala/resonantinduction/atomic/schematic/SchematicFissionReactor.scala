package resonantinduction.atomic.schematic

import java.util.HashMap

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.`type`.Pair
import resonant.lib.schematic.Schematic
import resonantinduction.atomic.AtomicContent
import resonant.lib.transform.vector.Vector3

class SchematicFissionReactor extends Schematic
{
    def getName: String =
    {
        return "schematic.fissionReactor.name"
    }

    def getStructure(dir: ForgeDirection, size: Int): HashMap[Vector3, Pair[Block, Integer]] =
    {
        val returnMap: HashMap[Vector3, Pair[Block, Integer]] = new HashMap[Vector3, Pair[Block, Integer]]
        if (size <= 1)
        {
            var r: Int = 2

            for (x <- -r to r; z <- -r to r)
            {
                val targetPosition: Vector3 = new Vector3(x, 0, z)
                returnMap.put(targetPosition, new Pair[Block, Integer](Blocks.water, 0))
            }

            r -= 1
            for (x <- -r to r; z <- -r to r)
            {
                val targetPosition: Vector3 = new Vector3(x, 1, z)
                returnMap.put(targetPosition, new Pair[Block, Integer](Block.getBlockFromName("electricTurbine"), 0))
                if (!((x == -r || x == r) && (z == -r || z == r)) && new Vector3(x, 0, z).magnitude <= 1)
                {
                    returnMap.put(new Vector3(x, -1, z), new Pair[Block, Integer](AtomicContent.blockControlRod, 0))
                    returnMap.put(new Vector3(x, -2, z), new Pair[Block, Integer](Blocks.sticky_piston, 1))
                }
            }

            returnMap.put(new Vector3(0, -1, 0), new Pair[Block, Integer](AtomicContent.blockThermometer, 0))
            returnMap.put(new Vector3(0, -3, 0), new Pair[Block, Integer](AtomicContent.blockSiren, 0))
            returnMap.put(new Vector3(0, -2, 0), new Pair[Block, Integer](Blocks.redstone_wire, 0))
            returnMap.put(new Vector3, new Pair[Block, Integer](AtomicContent.blockReactorCell, 0))
        }
        else
        {
            val r: Int = 2

            for (y <- 0 to size; x <- -r to r; z <- -r to r)
            {
                val targetPosition: Vector3 = new Vector3(x, y, z)
                val leveledPosition: Vector3 = new Vector3(0, y, 0)
                if (y < size - 1)
                {
                    if (targetPosition.distance(leveledPosition) == 2)
                    {
                        returnMap.put(targetPosition, new Pair[Block, Integer](AtomicContent.blockControlRod, 0))
                        var rotationMetadata: Int = 0
                        val offset: Vector3 = new Vector3(x, 0, z).normalize
                        for (checkDir <- ForgeDirection.VALID_DIRECTIONS)
                        {
                            if (offset.x == checkDir.offsetX && offset.y == checkDir.offsetY && offset.z == checkDir.offsetZ)
                            {
                                rotationMetadata = checkDir.getOpposite.ordinal
                            }
                        }
                        returnMap.put(targetPosition.clone.add(offset), new Pair[Block, Integer](Blocks.sticky_piston, rotationMetadata))
                    }
                    else if (x == -r || x == r || z == -r || z == r)
                    {
                        returnMap.put(targetPosition, new Pair[Block, Integer](Blocks.glass, 0))
                    }
                    else if (x == 0 && z == 0)
                    {
                        returnMap.put(targetPosition, new Pair[Block, Integer](AtomicContent.blockReactorCell, 0))
                    }
                    else
                    {
                        returnMap.put(targetPosition, new Pair[Block, Integer](Blocks.water, 0))
                    }
                }
                else if (targetPosition.distance(leveledPosition) < 2)
                {
                    returnMap.put(targetPosition, new Pair[Block, Integer](Block.getBlockFromName("electricTurbine"), 0))
                }
            }
        }
        return returnMap
    }
}
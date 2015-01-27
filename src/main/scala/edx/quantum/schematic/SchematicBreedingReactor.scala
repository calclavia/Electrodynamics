package edx.quantum.schematic

import java.util.HashMap

import edx.quantum.QuantumContent
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.lib.collection.Pair
import resonantengine.lib.schematic.Schematic
import resonantengine.lib.transform.vector.Vector3

class SchematicBreedingReactor extends Schematic
{
  override def getName: String =
  {
    return "schematic.breedingReactor.name"
  }

  override def getStructure(dir: ForgeDirection, size: Int): HashMap[Vector3, Pair[Block, Integer]] =
  {
    val returnMap: HashMap[Vector3, Pair[Block, Integer]] = new HashMap[Vector3, Pair[Block, Integer]]
    var r: Int = Math.max(size, 2)

    for (x <- -r to r)
    {
      for (z <- -r to r)
      {
        returnMap.put(new Vector3(x, 0, z), new Pair[Block, Integer](Blocks.water, 0))
      }
    }

    r -= 1

    for (x <- -r to r)
    {
      for (z <- -r to r)
      {
        val targetPosition: Vector3 = new Vector3(x, 1, z)
        if (new Vector3(x, 0, z).magnitude <= 2)
        {
          if (!((x == -r || x == r) && (z == -r || z == r)))
          {
            returnMap.put(new Vector3(x, 0, z), new Pair[Block, Integer](QuantumContent.blockReactorCell, 0))
            returnMap.put(new Vector3(x, -3, z), new Pair[Block, Integer](QuantumContent.blockSiren, 0))
            returnMap.put(new Vector3(x, -2, z), new Pair[Block, Integer](Blocks.redstone_wire, 0))
          }
          else
          {
            returnMap.put(new Vector3(x, -1, z), new Pair[Block, Integer](QuantumContent.blockControlRod, 0))
            returnMap.put(new Vector3(x, -2, z), new Pair[Block, Integer](Blocks.piston, 1))
          }
        }
      }
    }

    returnMap.put(new Vector3(0, -2, 0), new Pair[Block, Integer](Blocks.stone, 0))
    returnMap.put(new Vector3(0, -3, 0), new Pair[Block, Integer](Blocks.stone, 0))
    returnMap.put(new Vector3, new Pair[Block, Integer](QuantumContent.blockReactorCell, 0))
    return returnMap
  }

}
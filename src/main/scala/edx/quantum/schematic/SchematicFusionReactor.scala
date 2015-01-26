package edx.quantum.schematic

import java.util.HashMap

import edx.quantum.QuantumContent
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.lib.collection.Pair
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.schematic.Schematic

class SchematicFusionReactor extends Schematic
{
  override def getName: String =
  {
    return "schematic.fusionReactor.name"
  }

  def getStructure(dir: ForgeDirection, size: Int): HashMap[Vector3, Pair[Block, Integer]] =
  {
    val returnMap: HashMap[Vector3, Pair[Block, Integer]] = new HashMap[Vector3, Pair[Block, Integer]]
    val r: Int = size + 2
    for (y <- 0 to size; x <- -r to r; z <- -r to r)
    {
      val position: Vector3 = new Vector3(x, y, z)
      val magnitude: Double = Math.sqrt(x * x + z * z)
      if (!returnMap.containsKey(position))
      {
        returnMap.put(position, new Pair[Block, Integer](Blocks.air, 0))
      }
      if (magnitude <= r)
      {
        if (y == 0 || y == size)
        {
          if (magnitude >= 1)
          {
            val yDeviation: Double = (if (y == 0) size / 3 else -size / 3) + (if (y == 0) -1 else 1) * Math.sin(magnitude / r * Math.PI) * size / 2d
            val newPos: Vector3 = position.clone.add(0, yDeviation, 0)
            returnMap.put(newPos.round, new Pair[Block, Integer](QuantumContent.blockElectromagnet, 1))
          }
        }
        else if (magnitude > r - 1)
        {
          returnMap.put(position, new Pair[Block, Integer](QuantumContent.blockElectromagnet, 0))
        }
      }
    }

    for (y <- 0 to size)
    {
      returnMap.put(new Vector3(0, y, 0), new Pair[Block, Integer](QuantumContent.blockReactorCell, 0))
      returnMap.put(new Vector3(1, y, 0), new Pair[Block, Integer](QuantumContent.blockElectromagnet, 0))
      returnMap.put(new Vector3(0, y, 1), new Pair[Block, Integer](QuantumContent.blockElectromagnet, 0))
      returnMap.put(new Vector3(0, y, -1), new Pair[Block, Integer](QuantumContent.blockElectromagnet, 0))
      returnMap.put(new Vector3(-1, y, 0), new Pair[Block, Integer](QuantumContent.blockElectromagnet, 0))

    }
    returnMap.put(new Vector3(0, 0, 0), new Pair[Block, Integer](QuantumContent.blockReactorCell, 0))
    return returnMap
  }
}
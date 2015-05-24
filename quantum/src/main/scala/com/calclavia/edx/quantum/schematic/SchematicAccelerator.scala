package com.calclavia.edx.quantum.schematic

import java.util.HashMap

import com.calclavia.edx.quantum.QuantumContent
import QuantumContent
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.lib.collection.Pair
import resonantengine.lib.schematic.Schematic
import resonantengine.lib.transform.vector.Vector3

class SchematicAccelerator extends Schematic
{
  override def getName: String =
  {
    return "schematic.accelerator.name"
  }

  def getStructure(dir: ForgeDirection, size: Int): HashMap[Vector3, Pair[Block, Integer]] =
  {
    val returnMap: HashMap[Vector3, Pair[Block, Integer]] = new HashMap[Vector3, Pair[Block, Integer]]

    //Bottom
    returnMap.putAll(getBox(new Vector3(0, 0, 0), QuantumContent.blockElectromagnet, 1, size))
    returnMap.putAll(getBox(new Vector3(0, 0, 0), QuantumContent.blockElectromagnet, 0, size - 1))
    returnMap.putAll(getBox(new Vector3(0, 0, 0), QuantumContent.blockElectromagnet, 0, size + 1))
    //Mid
    returnMap.putAll(getBox(new Vector3(0, 1, 0), Blocks.air, 0, size))
    returnMap.putAll(getBox(new Vector3(0, 1, 0), QuantumContent.blockElectromagnet, 1, size - 1))
    returnMap.putAll(getBox(new Vector3(0, 1, 0), QuantumContent.blockElectromagnet, 1, size + 1))
    //Top
    returnMap.putAll(getBox(new Vector3(0, 2, 0), QuantumContent.blockElectromagnet, 1, size))
    returnMap.putAll(getBox(new Vector3(0, 2, 0), QuantumContent.blockElectromagnet, 0, size - 1))
    returnMap.putAll(getBox(new Vector3(0, 2, 0), QuantumContent.blockElectromagnet, 0, size + 1))

    return returnMap
  }
}
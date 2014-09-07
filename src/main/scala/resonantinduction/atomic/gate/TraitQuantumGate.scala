package resonantinduction.atomic.gate

import codechicken.multipart.TileMultipart
import resonant.api.blocks.IBlockFrequency

class TraitQuantumGate extends TileMultipart with IQuantumGate
{

  def transport(entity: Any)
  {
    get.transport(entity)
  }

  def get: PartQuantumGlyph =
  {
    for (part <- partList)
    {
      if (part.isInstanceOf[PartQuantumGlyph])
      {
        return (part.asInstanceOf[PartQuantumGlyph])
      }
    }
    return null
  }

  def getFrequency: Int =
  {
    var frequency: Int = 0
    var i: Int = 0
    for (part <- partList)
    {
      if (part.isInstanceOf[IQuantumGate])
      {
        frequency += (Math.pow(PartQuantumGlyph.MAX_GLYPH, i) * (part.asInstanceOf[IBlockFrequency]).getFrequency).asInstanceOf[Int]
        i += 1
      }
    }
    if (i >= 8) return frequency
    return -1
  }

  def setFrequency(frequency: Int)
  {
  }
}
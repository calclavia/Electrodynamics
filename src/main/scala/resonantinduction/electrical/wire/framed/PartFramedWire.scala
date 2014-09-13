package resonantinduction.electrical.wire.framed

import resonantinduction.electrical.wire.base.TWire

/**
 * A framed version of the electrical wire
 * @author Calclavia
 */
class PartFramedWire extends TWire
{
  def preparePlacement(side: Int, meta: Int)
  {
    setMaterial(meta)
  }

  override def getType = "FramedWire"
}

package resonantinduction.electrical.wire.framed

import resonantinduction.core.prefab.part.connector.PartFramedNode
import resonantinduction.electrical.wire.base.TWire

/**
 * A framed version of the electrical wire
 * @author Calclavia
 */
class PartFramedWire extends PartFramedNode with TWire
{
  def preparePlacement(side: Int, meta: Int)
  {
    setMaterial(meta)
  }
}

package resonantinduction.electrical.wire.framed

import net.minecraft.item.ItemStack
import resonantinduction.core.prefab.part.{TColorable, PartFramedNode}
import resonantinduction.electrical.ElectricalContent
import resonantinduction.electrical.wire.base.{WireMaterial, TWire}

/**
 * A framed version of the electrical wire
 * @author Calclavia
 */
class PartFramedWire extends TColorable[WireMaterial](ElectricalContent.itemInsulation) with TWire
{
  def preparePlacement(side: Int, meta: Int)
  {
    setMaterial(meta)
  }

  override def getType = "FramedWire"
}

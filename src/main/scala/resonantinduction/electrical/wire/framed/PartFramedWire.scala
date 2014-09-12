package resonantinduction.electrical.wire.framed

import net.minecraft.item.ItemStack
import resonantinduction.core.prefab.part.{PartColorableMaterial, PartFramedNode}
import resonantinduction.electrical.ElectricalContent
import resonantinduction.electrical.wire.WireMaterial
import resonantinduction.electrical.wire.base.TWire

/**
 * A framed version of the electrical wire
 * @author Calclavia
 */
class PartFramedWire extends PartColorableMaterial[WireMaterial](ElectricalContent.itemInsulation) with TWire
{
  override def getType = "FramedWire"
}

package resonantinduction.electrical.wire.framed

import net.minecraft.item.ItemStack
import resonantinduction.core.prefab.part.PartFramedNode
import resonantinduction.electrical.ElectricalContent
import resonantinduction.electrical.wire.WireMaterial

/**
 * A framed version of the electrical wire
 * @author Calclavia
 */
class PartFramedWire extends PartFramedNode[WireMaterial](ElectricalContent.itemInsulation)
{
  override def setMaterial(i: Int)
  {
    material = WireMaterial.values()(i)
  }

  override def getMaterialID = material.ordinal()

  override protected def getItem = new ItemStack(ElectricalContent.itemInsulation, getMaterialID)

  override def getType = "FramedWire"
}

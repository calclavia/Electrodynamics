package resonantinduction.electrical.wire.base

import net.minecraft.item.{Item, ItemStack}
import resonantinduction.core.prefab.part.{TMaterial, TInsulatable, TColorable}
import resonantinduction.electrical.ElectricalContent
import resonantinduction.electrical.wire.WireMaterial

/**
 * Trait implemented by wires
 * @author Calclavia
 */
trait TWire extends TColorable with TMaterial[WireMaterial] with TInsulatable
{
  override protected val insulationItem: Item = ElectricalContent.itemInsulation

  def preparePlacement(side: Int, meta: Int)

  override def setMaterial(i: Int)
  {
    material = WireMaterial.values()(i)
  }

  override def getMaterialID = material.ordinal()

  override protected def getItem = new ItemStack(ElectricalContent.itemInsulation, getMaterialID)

}

package resonantinduction.electrical.wire.base

import net.minecraft.item.ItemStack
import resonantinduction.core.prefab.part.TColorable
import resonantinduction.electrical.ElectricalContent
import resonantinduction.electrical.wire.WireMaterial

/**
 * @author Calclavia
 */
trait TWire extends TColorable[WireMaterial]
{
  def preparePlacement(side: Int, meta: Int)

  override def setMaterial(i: Int)
  {
    material = WireMaterial.values()(i)
  }

  override def getMaterialID = material.ordinal()

  override protected def getItem = new ItemStack(ElectricalContent.itemInsulation, getMaterialID)

}

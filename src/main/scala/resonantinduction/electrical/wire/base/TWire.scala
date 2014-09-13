package resonantinduction.electrical.wire.base

import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.prefab.part.{TColorable, TInsulatable, TMaterial}
import resonantinduction.electrical.ElectricalContent
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.simulator.dc.DCNode

/**
 * Trait implemented by wires
 * @author Calclavia
 */
abstract class TWire extends TColorable with TMaterial[WireMaterial] with TInsulatable
{
  override protected val insulationItem: Item = ElectricalContent.itemInsulation

  def preparePlacement(side: Int, meta: Int)

  override def setMaterial(i: Int)
  {
    material = WireMaterial.values()(i)
  }

  override def getMaterialID = material.ordinal()

  override protected def getItem = new ItemStack(ElectricalContent.itemInsulation, getMaterialID)

  /**
   * Can this conductor connect with another potential wire object?
   */
  protected def canConnectTo(obj: AnyRef): Boolean =
  {
    if (obj != null && obj.getClass == getClass)
    {
      val wire = obj.asInstanceOf[TWire]

      if (material == wire.material)
      {
        if (insulated && wire.insulated)
          return this.getColor == wire.getColor || (getColor == defaultColor || wire.getColor == defaultColor)

        return true
      }
    }

    return false
  }

  /**
   * Can this conductor connect with another potential wire object AND a DCNode?
   */
  protected def canConnectTo(obj: AnyRef, from: ForgeDirection): Boolean =
  {
    if (canConnectTo(obj))
      return true
    else if (obj.isInstanceOf[INodeProvider])
    {
      val node = obj.asInstanceOf[INodeProvider].getNode(classOf[DCNode], from)

      if (node != null)
        return node.asInstanceOf[DCNode].canConnect(from)
    }

    return false
  }
}

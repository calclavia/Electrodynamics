package resonantinduction.electrical.wire.base

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.multipart.TMultiPart
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.prefab.part.connector._
import resonantinduction.electrical.ElectricalContent
import universalelectricity.api.core.grid.{INode, INodeProvider}
import universalelectricity.simulator.dc.DCNode

/**
 * Abstract class extended by both flat and framed wires to handle material, insulation, color and multipart node logic.
 * @author Calclavia
 */
abstract class TWire extends TMultiPart with TNodePartConnector with TPart with TMaterial[WireMaterial] with TInsulatable with TColorable
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
   * Packet Methods
   */

  override def read(packet: MCDataInput, packetID: Int)
  {
    super[TMaterial].read(packet)
    super[TInsulatable].read(packet)
    super[TColorable].read(packet)
  }

  override def readDesc(packet: MCDataInput)
  {
    super[TMaterial].readDesc(packet)
    super[TInsulatable].readDesc(packet)
    super[TColorable].readDesc(packet)
  }

  override def writeDesc(packet: MCDataOutput)
  {
    super[TMaterial].writeDesc(packet)
    super[TInsulatable].writeDesc(packet)
    super[TColorable].writeDesc(packet)
  }

  /**
   * NBT Methods
   */
  override def load(nbt: NBTTagCompound)
  {
    super[TMaterial].load(nbt)
    super[TInsulatable].load(nbt)
    super[TColorable].load(nbt)
  }

  override def save(nbt: NBTTagCompound)
  {
    super[TMaterial].save(nbt)
    super[TInsulatable].save(nbt)
    super[TColorable].save(nbt)
  }

  /**
   * Can this conductor connect with another potential wire object?
   */
  def canConnectTo(obj: AnyRef): Boolean =
  {
    if (obj != null && obj.getClass == getClass)
    {
      val wire = obj.asInstanceOf[TWire]

      if (material == wire.material)
      {
        if (insulated && wire.insulated)
          return this.getColor == wire.getColor || (getColor == TColorable.defaultColor || wire.getColor == TColorable.defaultColor)

        return true
      }
    }

    return false
  }

  /**
   * Can this conductor connect with another potential wire object AND a DCNode?
   */
  def canConnectTo(obj: AnyRef, from: ForgeDirection): Boolean =
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

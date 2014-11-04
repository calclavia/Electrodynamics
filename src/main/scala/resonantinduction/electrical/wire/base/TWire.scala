package resonantinduction.electrical.wire.base

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.prefab.part.connector._
import resonantinduction.electrical.ElectricalContent
import resonant.api.grid.INodeProvider
import resonant.lib.grid.node.DCNode

/**
 * Abstract class extended by both flat and framed wires to handle material, insulation, color and multipart node logic.
 *
 * Packets:
 * 0 - Desc
 * 1 - Material
 * 2 - Insulation
 * 3 - Color
 *
 * @author Calclavia
 */
trait TWire extends PartAbstract with TNodePartConnector with TMaterial[WireMaterial] with TInsulatable with TColorable
{
  override protected val insulationItem: Item = ElectricalContent.itemInsulation
  material = WireMaterial.COPPER

  def preparePlacement(side: Int, meta: Int)

  override def setMaterial(i: Int)
  {
    material = WireMaterial.values()(i)
  }

  override def getMaterialID = material.ordinal()

  override protected def getItem = new ItemStack(ElectricalContent.itemWire, getMaterialID)

  /**
   * Packet Methods
   */
  override def writeDesc(packet: MCDataOutput)
  {
    super[TMaterial].writeDesc(packet)
    super[TInsulatable].writeDesc(packet)
    super[TColorable].writeDesc(packet)
  }

  override def readDesc(packet: MCDataInput)
  {
    super[TMaterial].readDesc(packet)
    super[TInsulatable].readDesc(packet)
    super[TColorable].readDesc(packet)
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    super[TInsulatable].read(packet, packetID)
    super[TColorable].read(packet, packetID)
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
}

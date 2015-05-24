package com.calclavia.edx.electrical.circuit.wire.base

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import com.calclavia.edx.electrical.ElectricalContent
import edx.core.prefab.part.connector._
import ElectricalContent
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

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
trait TWire extends PartAbstract with TPartNodeProvider with TMaterial[WireMaterial] with TInsulatable with TColorable
{
  override protected val insulationItem: Item = ElectricalContent.itemInsulation

  material = WireMaterial.COPPER

  def preparePlacement(side: Int, meta: Int)

  override def setMaterial(i: Int)
  {
    material = WireMaterial.values()(i)
  }

  /**
   * Packet Methods
   */
  override def write(packet: MCDataOutput, id: Int)
  {
    super[TMaterial].write(packet, id)
    super[TInsulatable].write(packet, id)
    super[TColorable].write(packet, id)
  }

  override def read(packet: MCDataInput, id: Int)
  {
    super[TMaterial].read(packet, id)
    super[TInsulatable].read(packet, id)
    super[TColorable].read(packet, id)
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

  override protected def getItem = new ItemStack(ElectricalContent.itemWire, getMaterialID)

  override def getMaterialID = material.ordinal()
}

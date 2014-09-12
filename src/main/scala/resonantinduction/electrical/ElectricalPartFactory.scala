package resonantinduction.electrical

import codechicken.multipart.MultiPartRegistry.IPartFactory
import codechicken.multipart.{MultiPartRegistry, TMultiPart}
import resonantinduction.atomic.gate.PartQuantumGlyph
import resonantinduction.electrical.levitator.PartLevitator
import resonantinduction.electrical.multimeter.PartMultimeter
import resonantinduction.electrical.transformer.PartElectricTransformer
import resonantinduction.electrical.wire.flat.PartFlatWire
import resonantinduction.electrical.wire.framed.PartFramedWire

object ElectricalPartFactory extends IPartFactory
{
  MultiPartRegistry.registerParts(this, Array("resonant_induction_wire", "resonant_induction_switch_wire", "resonant_induction_flat_wire", "resonant_induction_flat_switch_wire", "resonant_induction_multimeter", "resonant_induction_transformer", "resonant_induction_charger", "resonant_induction_levitator"))

  def createPart(name: String, client: Boolean): TMultiPart =
  {
    name match
    {
      case "resonant_induction_framed_wire" => new PartFramedWire
      case "resonant_induction_flat_wire" => new PartFlatWire
      case "resonant_induction_multimeter" => new PartMultimeter
      case "resonant_induction_transformer" => new PartElectricTransformer
      case "resonant_induction_levitator" => new PartLevitator
      case "resonant_induction_quantum_glyph" => new PartQuantumGlyph
      case _ => null
    }
  }
}
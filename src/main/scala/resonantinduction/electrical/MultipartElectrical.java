package resonantinduction.electrical;

import resonantinduction.electrical.levitator.PartLevitator;
import resonantinduction.electrical.multimeter.PartMultimeter;
import resonantinduction.electrical.transformer.PartElectricTransformer;
import resonantinduction.atomic.gate.PartQuantumGlyph;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartElectrical implements IPartFactory
{
	public static MultipartElectrical INSTANCE;

	public static final String[] PART_TYPES = { "resonant_induction_quantum_glyph", "resonant_induction_wire", "resonant_induction_switch_wire", "resonant_induction_flat_wire", "resonant_induction_flat_switch_wire", "resonant_induction_multimeter", "resonant_induction_transformer", "resonant_induction_charger", "resonant_induction_levitator", "resonant_induction_itemrailing" };

	public MultipartElectrical()
	{
		MultiPartRegistry.registerParts(this, PART_TYPES);
		MultipartGenerator.registerPassThroughInterface("universalelectricity.api.electricity.IVoltageOutput");
		MultipartGenerator.registerTrait("IQuantumGate", "TraitQuantumGate");
		MultipartGenerator.registerTrait("universalelectricity.api.energy.IConductor", "TraitConductor");
		MultipartGenerator.registerTrait("cofh.api.energy.IEnergyHandler", "TraitEnergyHandler");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "TraitEnergySink");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		//if (name.equals("resonant_induction_wire"))
			//return new PartFramedWire();
		//else if (name.equals("resonant_induction_switch_wire"))
			//return new PartFramedSwitchWire();
		//if (name.equals("resonant_induction_flat_wire"))
			//return new PartFlatWire();
		//else if (name.equals("resonant_induction_flat_switch_wire"))
			//return new PartFlatSwitchWire();
		if (name.equals("resonant_induction_multimeter"))
			return new PartMultimeter();
		else if (name.equals("resonant_induction_transformer"))
			return new PartElectricTransformer();
		else if (name.equals("resonant_induction_levitator"))
			return new PartLevitator();
		else if (name.equals("resonant_induction_quantum_glyph"))
			return new PartQuantumGlyph();

		return null;
	}
}

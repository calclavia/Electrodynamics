package resonantinduction.electrical;

import resonantinduction.electrical.multimeter.PartMultimeter;
import resonantinduction.electrical.transformer.PartTransformer;
import resonantinduction.electrical.wire.flat.PartFlatSwitchWire;
import resonantinduction.electrical.wire.flat.PartFlatWire;
import resonantinduction.electrical.wire.framed.PartFramedSwitchWire;
import resonantinduction.electrical.wire.framed.PartFramedWire;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartElectrical implements IPartFactory
{
	public static MultipartElectrical INSTANCE;

	public static final String[] PART_TYPES = { "resonant_induction_wire", "resonant_induction_switch_wire", "resonant_induction_flat_wire", "resonant_induction_flat_switch_wire", "resonant_induction_multimeter", "resonant_induction_transformer" };

	public MultipartElectrical()
	{
		MultiPartRegistry.registerParts(this, PART_TYPES);
		MultipartGenerator.registerTrait("universalelectricity.api.energy.IConductor", "TraitConductor");
		MultipartGenerator.registerTrait("cofh.api.energy.IEnergyHandler", "TraitEnergyHandler");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "TraitEnergySink");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		switch (name)
		{
			case "resonant_induction_wire":
				return new PartFramedWire();
			case "resonant_induction_switch_wire":
				return new PartFramedSwitchWire();
			case "resonant_induction_flat_wire":
				return new PartFlatWire();
			case "resonant_induction_flat_switch_wire":
				return new PartFlatSwitchWire();
			case "resonant_induction_multimeter":
				return new PartMultimeter();
			case "resonant_induction_transformer":
				return new PartTransformer();
		}

		return null;
	}
}

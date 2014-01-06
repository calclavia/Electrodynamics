package resonantinduction;

import ic2.api.energy.tile.IEnergySink;
import resonantinduction.transport.transformer.PartTransformer;
import resonantinduction.transport.wire.flat.PartFlatSwitchWire;
import resonantinduction.transport.wire.flat.PartFlatWire;
import resonantinduction.transport.wire.framed.PartFramedSwitchWire;
import resonantinduction.transport.wire.framed.PartFramedWire;
import resonantinduction.transport.wire.trait.TraitConductor;
import resonantinduction.transport.wire.trait.TraitEnergyHandler;
import resonantinduction.transport.wire.trait.TraitEnergySink;
import resonantinduction.utility.multimeter.PartMultimeter;
import universalelectricity.api.energy.IConductor;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;
import cofh.api.energy.IEnergyHandler;

public class MultipartRI implements IPartFactory
{
	public static MultipartRI INSTANCE;

	public static final String[] PART_TYPES = { "resonant_induction_wire", "resonant_induction_switch_wire", "resonant_induction_flat_wire", "resonant_induction_flat_switch_wire", "resonant_induction_multimeter", "resonant_induction_transformer" };

	public MultipartRI()
	{
		MultiPartRegistry.registerParts(this, PART_TYPES);
		MultipartGenerator.registerTrait(IConductor.class.getCanonicalName(), TraitConductor.class.getCanonicalName());
		MultipartGenerator.registerTrait(IEnergyHandler.class.getCanonicalName(), TraitEnergyHandler.class.getCanonicalName());
		MultipartGenerator.registerTrait(IEnergySink.class.getCanonicalName(), TraitEnergySink.class.getCanonicalName());
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name == "resonant_induction_wire")
		{
			return new PartFramedWire();
		}
		else if (name == "resonant_induction_switch_wire")
		{
			return new PartFramedSwitchWire();
		}

		else if (name == "resonant_induction_flat_wire")
		{
			return new PartFlatWire();
		}
		else if (name == "resonant_induction_flat_switch_wire")
		{
			return new PartFlatSwitchWire();
		}
		else if (name == "resonant_induction_multimeter")
		{
			return new PartMultimeter();
		}
		else if (name == "resonant_induction_transformer")
		{
			return new PartTransformer();
		}

		return null;
	}
}

package resonantinduction;

import resonantinduction.multimeter.PartMultimeter;
import resonantinduction.transformer.PartTransformer;
import resonantinduction.wire.part.PartFlatWire;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartRI implements IPartFactory
{
	public static MultipartRI INSTANCE;

	public MultipartRI()
	{
		MultiPartRegistry.registerParts(this, new String[] { "resonant_induction_flat_wire", "resonant_induction_multimeter", "resonant_induction_transformer" });
		MultipartGenerator.registerTrait("universalelectricity.api.energy.IConductor", "resonantinduction.wire.part.TraitConductor");
		MultipartGenerator.registerTrait("cofh.api.energy.IEnergyHandler", "resonantinduction.wire.part.TraitEnergyHandler");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "resonantinduction.wire.part.TraitEnergySink");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name.equals("resonant_induction_flat_wire"))
		{
			return new PartFlatWire();
		}
		else if (name.equals("resonant_induction_multimeter"))
		{
			return new PartMultimeter();
		}
		else if (name.equals("resonant_induction_transformer"))
		{
			return new PartTransformer();
		}

		return null;
	}
}

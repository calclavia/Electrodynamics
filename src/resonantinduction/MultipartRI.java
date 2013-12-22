package resonantinduction;

import resonantinduction.wire.part.FlatWire;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartRI implements IPartFactory
{
	public static MultipartRI INSTANCE;

	public MultipartRI()
	{
		MultiPartRegistry.registerParts(this, new String[] { "resonant_induction_flat_wire" });
		MultipartGenerator.registerTrait("universalelectricity.api.energy.IConductor", "resonantinduction.wire.part.TraitConductor");
		//MultipartGenerator.registerTrait("resonantinduction.wire.part.ITest", "resonantinduction.wire.part.TraitTest");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "resonantinduction.wire.part.TraitEnergySink");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name.equals("resonant_induction_flat_wire"))
		{
			return new FlatWire();
		}

		return null;
	}
}

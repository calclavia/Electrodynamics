package resonantinduction;

import resonantinduction.wire.IAdvancedConductor;
import resonantinduction.wire.IBlockableConnection;
import resonantinduction.wire.part.PartFlatWire;
import buildcraft.api.power.IPowerReceptor;
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
		MultipartGenerator.registerPassThroughInterface(IAdvancedConductor.class.getName());
		MultipartGenerator.registerPassThroughInterface(IPowerReceptor.class.getName());
		MultipartGenerator.registerPassThroughInterface(IBlockableConnection.class.getName());
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "resonantinduction.wire.TEnergySink");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name.equals("resonant_induction_flat_wire"))
		{
			return new PartFlatWire();
		}

		return null;
	}
}

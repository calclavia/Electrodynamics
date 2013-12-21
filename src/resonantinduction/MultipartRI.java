package resonantinduction;

import buildcraft.api.power.IPowerReceptor;
import resonantinduction.wire.IBlockableConnection;
import resonantinduction.wire.IInsulatedMaterial;
import resonantinduction.wire.part.PartFlatWire;
import resonantinduction.wire.part.PartWire;
import universalelectricity.api.energy.IConductor;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartRI implements IPartFactory
{
	public static MultipartRI INSTANCE;

	public MultipartRI()
	{
		MultiPartRegistry.registerParts(this, new String[] {/* "resonant_induction_wire",*/ "resonant_induction_flat_wire" });
		MultipartGenerator.registerPassThroughInterface(IConductor.class.getName());
		MultipartGenerator.registerPassThroughInterface(IPowerReceptor.class.getName());
		MultipartGenerator.registerPassThroughInterface(IInsulatedMaterial.class.getName());
		MultipartGenerator.registerPassThroughInterface(IBlockableConnection.class.getName());
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "resonantinduction.wire.TEnergySink");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		/*if (name.equals("resonant_induction_wire"))
		{
			return new PartWire();
		}
		else */
		if (name.equals("resonant_induction_flat_wire"))
		{
			return new PartFlatWire();
		}

		return null;
	}
}

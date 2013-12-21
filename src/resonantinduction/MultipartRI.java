package resonantinduction;

import resonantinduction.wire.part.PartLainWire;
import resonantinduction.wire.part.PartWire;
import universalelectricity.api.energy.IConductor;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartRI implements IPartFactory
{

	public MultipartRI()
	{
		this.init();
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name == "resonant_induction_wire")
			return new PartWire();
		if (name == "resonant_induction_lain_wire")
			return new PartLainWire();
		return null;
	}

	public void init()
	{
		MultiPartRegistry.registerParts(this, new String[] { "resonant_induction_wire", "resonant_induction_lain_wire" });
		MultipartGenerator.registerPassThroughInterface(IConductor.class.getName());
		MultipartGenerator.registerPassThroughInterface("buildcraft.api.power.IPowerReceptor");
		MultipartGenerator.registerPassThroughInterface("resonantinduction.wire.IInsulatedMaterial");
		MultipartGenerator.registerPassThroughInterface("resonantinduction.wire.IBlockableConnection");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "resonantinduction.wire.TEnergySink");
	}

}

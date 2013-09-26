package resonantinduction;

import resonantinduction.wire.EnumWireMaterial;
import resonantinduction.wire.multipart.PartWire;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartRI implements IPartFactory
{
	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name == "resonant_induction_wire")
			return new PartWire(EnumWireMaterial.COPPER.ordinal());
		return null;
	}
	
	public void init()
	{
		MultiPartRegistry.registerParts(this, new String[]{"resonant_induction_wire"});
		MultipartGenerator.registerPassThroughInterface("universalelectricity.core.block.IConductor");
		MultipartGenerator.registerPassThroughInterface("ic2.api.energy.tile.IEnergySink");
		MultipartGenerator.registerPassThroughInterface("buildcraft.api.power.IPowerReceptor");
		MultipartGenerator.registerPassThroughInterface("resonantinduction.wire.IInsulatedMaterial");
	}

}

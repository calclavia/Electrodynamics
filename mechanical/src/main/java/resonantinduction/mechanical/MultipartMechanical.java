package resonantinduction.mechanical;

import resonantinduction.mechanical.energy.gear.PartGear;
import resonantinduction.mechanical.energy.gear.PartGearShaft;
import resonantinduction.mechanical.fluid.pipe.PartPipe;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartMechanical implements IPartFactory
{
	public static MultipartMechanical INSTANCE;

	public static final String[] PART_TYPES = { "resonant_induction_gear", "resonant_induction_gear_shaft", "resonant_induction_pipe" };

	public MultipartMechanical()
	{
		MultiPartRegistry.registerParts(this, PART_TYPES);
		MultipartGenerator.registerPassThroughInterface("resonantinduction.core.grid.fluid.IPressureNodeProvider");
		// TODO: Move to UE
		MultipartGenerator.registerTrait("resonantinduction.core.grid.INodeProvider", "resonantinduction.core.grid.TraitNodeProvider");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name.equals("resonant_induction_gear"))
		{
			return new PartGear();
		}
		else if (name.equals("resonant_induction_gear_shaft"))
		{
			return new PartGearShaft();
		}
		else if (name.equals("resonant_induction_pipe"))
		{
			return new PartPipe();
		}

		return null;
	}
}

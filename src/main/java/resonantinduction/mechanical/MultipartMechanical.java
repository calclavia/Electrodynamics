package resonantinduction.mechanical;

import resonantinduction.mechanical.gear.PartGear;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;

public class MultipartMechanical implements IPartFactory
{
	public static MultipartMechanical INSTANCE;

	public static final String[] PART_TYPES = { "resonant_induction_gear" };

	public MultipartMechanical()
	{
		MultiPartRegistry.registerParts(this, PART_TYPES);
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		switch (name)
		{
			case "resonant_induction_gear":
				return new PartGear();
		}

		return null;
	}
}

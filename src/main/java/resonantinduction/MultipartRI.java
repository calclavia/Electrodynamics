package resonantinduction;

import resonantinduction.multimeter.PartMultimeter;
import resonantinduction.transformer.PartTransformer;
import resonantinduction.wire.flat.PartFlatSwitchWire;
import resonantinduction.wire.flat.PartFlatWire;
import resonantinduction.wire.framed.PartWire;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartRI implements IPartFactory
{
	public static MultipartRI INSTANCE;
	
	public static final String[] PART_TYPES = {
	                                           "resonant_induction_wire",
	                                           "resonant_induction_flat_wire", 
	                                           "resonant_induction_flat_switch_wire", 
	                                           "resonant_induction_multimeter", 
	                                           "resonant_induction_transformer"
	};

	public MultipartRI()
	{
		MultiPartRegistry.registerParts(this, PART_TYPES);
		MultipartGenerator.registerTrait("universalelectricity.api.energy.IConductor", "resonantinduction.wire.trait.TraitConductor");
		MultipartGenerator.registerTrait("cofh.api.energy.IEnergyHandler", "resonantinduction.wire.trait.TraitEnergyHandler");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "resonantinduction.wire.trait.TraitEnergySink");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
	    switch(name)
	    {
	        case "resonant_induction_wire":
	            return new PartWire();
	            
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

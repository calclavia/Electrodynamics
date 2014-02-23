package resonantinduction.core.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIResonantInductionConfig implements IConfigureNEI
{

	@Override
	public void loadConfig()
	{
		API.registerRecipeHandler(new RIGrinderRecipeHandler());
		API.registerUsageHandler(new RIGrinderRecipeHandler());
		
		API.registerRecipeHandler(new RICrusherRecipeHandler());
		API.registerUsageHandler(new RICrusherRecipeHandler());
		
		API.registerRecipeHandler(new RIMixerRecipeHandler());
		API.registerUsageHandler(new RIMixerRecipeHandler());
		
		API.registerRecipeHandler(new RISawmillRecipeHandler());
		API.registerUsageHandler(new RISawmillRecipeHandler());
		
		API.registerRecipeHandler(new RISmelterRecipeHandler());
		API.registerUsageHandler(new RISmelterRecipeHandler());
	}

	@Override
	public String getName()
	{
		return "Resonant Induction Plugin";
	}

	@Override
	public String getVersion()
	{
		return "1.0";
	}

}

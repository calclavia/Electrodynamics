package resonantinduction.core.nei;

import net.minecraft.block.Block;
import resonantinduction.core.ResonantInduction;
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

		for (Block block : ResonantInduction.blockMixtureFluids.values())
			API.hideItem(block.blockID);

		for (Block block : ResonantInduction.blockMoltenFluid.values())
			API.hideItem(block.blockID);

		API.hideItem(ResonantInduction.blockDust.blockID);
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

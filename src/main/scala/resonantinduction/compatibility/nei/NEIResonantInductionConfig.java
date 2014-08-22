package resonantinduction.compatibility.nei;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import resonant.engine.ResonantEngine;
import resonantinduction.core.CoreContent;
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

		for (Block block : ResonantEngine.resourceFactory.mixtureFactory.blockMixtureFluids.values())
			API.hideItem(new ItemStack(block));

		for (Block block : ResonantEngine.resourceFactory.moltenFactory.blockMoltenFluids.values())
			API.hideItem(new ItemStack(block));

		//API.hideItem(new ItemStack(CoreContent.blockDust()));
		//API.hideItem(new ItemStack(CoreContent.blockRefinedDust()));
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

package resonantinduction.compatibility.nei

import codechicken.nei.api.{API, IConfigureNEI}
import net.minecraft.item.ItemStack
import resonant.engine.ResonantEngine

class NEIResonantInductionConfig extends IConfigureNEI
{
    def loadConfig
    {
        API.registerRecipeHandler(new RIGrinderRecipeHandler)
        API.registerUsageHandler(new RIGrinderRecipeHandler)
        API.registerRecipeHandler(new RICrusherRecipeHandler)
        API.registerUsageHandler(new RICrusherRecipeHandler)
        API.registerRecipeHandler(new RIMixerRecipeHandler)
        API.registerUsageHandler(new RIMixerRecipeHandler)
        API.registerRecipeHandler(new RISawmillRecipeHandler)
        API.registerUsageHandler(new RISawmillRecipeHandler)
        API.registerRecipeHandler(new RISmelterRecipeHandler)
        API.registerUsageHandler(new RISmelterRecipeHandler)
        import scala.collection.JavaConversions._
        for (block <- ResonantEngine.resourceFactory.mixtureFactory.blockMixtureFluids.values) API.hideItem(new ItemStack(block))
        import scala.collection.JavaConversions._
        for (block <- ResonantEngine.resourceFactory.moltenFactory.blockMoltenFluids.values) API.hideItem(new ItemStack(block))
    }

    def getName: String =
    {
        return "Resonant Induction Plugin"
    }

    def getVersion: String =
    {
        return "1.0"
    }
}
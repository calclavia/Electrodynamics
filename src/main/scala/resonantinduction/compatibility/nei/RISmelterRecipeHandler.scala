package resonantinduction.compatibility.nei

import resonant.lib.utility.LanguageUtility
import resonant.lib.factory.resources.RecipeType

class RISmelterRecipeHandler extends RITemplateRecipeHandler
{
    def getRecipeName: String =
    {
        return LanguageUtility.getLocal("resonantinduction.machine.smelter")
    }

    def getMachine: RecipeType =
    {
        return RecipeType.SMELTER
    }
}
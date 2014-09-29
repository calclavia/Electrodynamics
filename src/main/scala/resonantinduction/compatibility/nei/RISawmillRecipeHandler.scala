package resonantinduction.compatibility.nei

import resonant.lib.utility.LanguageUtility
import resonant.content.factory.resources.RecipeType

class RISawmillRecipeHandler extends RITemplateRecipeHandler
{
    def getRecipeName: String =
    {
        return LanguageUtility.getLocal("resonantinduction.machine.sawmill")
    }

    def getMachine: RecipeType =
    {
        return RecipeType.SAWMILL
    }
}
package resonantinduction.compatibility.nei

import resonant.lib.utility.LanguageUtility
import resonant.content.factory.resources.RecipeType

class RIGrinderRecipeHandler extends RITemplateRecipeHandler
{
    def getRecipeName: String =
    {
        return LanguageUtility.getLocal("resonantinduction.machine.grinder")
    }

    def getMachine: RecipeType =
    {
        return RecipeType.GRINDER
    }
}
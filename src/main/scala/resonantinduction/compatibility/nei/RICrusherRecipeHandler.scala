package resonantinduction.compatibility.nei

import resonant.lib.utility.LanguageUtility
import resonant.content.factory.resources.RecipeType

class RICrusherRecipeHandler extends RITemplateRecipeHandler
{
    def getRecipeName: String =
    {
        return LanguageUtility.getLocal("resonantinduction.machine.crusher")
    }

    def getMachine: RecipeType =
    {
        return RecipeType.CRUSHER
    }
}
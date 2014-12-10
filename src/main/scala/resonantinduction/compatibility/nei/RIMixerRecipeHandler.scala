package resonantinduction.compatibility.nei

import resonant.lib.utility.LanguageUtility
import resonant.lib.factory.resources.RecipeType

class RIMixerRecipeHandler extends RITemplateRecipeHandler
{
    def getRecipeName: String =
    {
        return LanguageUtility.getLocal("resonantinduction.machine.mixer")
    }

    def getMachine: RecipeType =
    {
        return RecipeType.MIXER
    }
}
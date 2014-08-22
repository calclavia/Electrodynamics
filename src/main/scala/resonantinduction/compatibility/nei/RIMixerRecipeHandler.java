package resonantinduction.compatibility.nei;

import resonant.lib.utility.LanguageUtility;
import resonant.content.factory.resources.RecipeType;

public class RIMixerRecipeHandler extends RITemplateRecipeHandler
{

	@Override
	public String getRecipeName()
	{
		return LanguageUtility.getLocal("resonantinduction.machine.mixer");
	}

	@Override
	public RecipeType getMachine()
	{
		return RecipeType.MIXER;
	}
}

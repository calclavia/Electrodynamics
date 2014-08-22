package resonantinduction.compatibility.nei;

import resonant.lib.utility.LanguageUtility;
import resonant.content.factory.resources.RecipeType;

public class RISmelterRecipeHandler extends RITemplateRecipeHandler
{

	@Override
	public String getRecipeName()
	{
		return LanguageUtility.getLocal("resonantinduction.machine.smelter");
	}

	@Override
	public RecipeType getMachine()
	{
		return RecipeType.SMELTER;
	}
}

package resonantinduction.core.nei;

import resonant.lib.utility.LanguageUtility;
import resonantinduction.core.ResonantInduction.RecipeType;

public class RISawmillRecipeHandler extends RITemplateRecipeHandler
{

	@Override
	public String getRecipeName()
	{
		return LanguageUtility.getLocal("resonantinduction.machine.sawmill");
	}

	@Override
	public RecipeType getMachine()
	{
		return RecipeType.SAWMILL;
	}
}

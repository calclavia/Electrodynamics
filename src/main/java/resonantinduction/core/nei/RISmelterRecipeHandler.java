package resonantinduction.core.nei;

import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import calclavia.lib.utility.LanguageUtility;

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

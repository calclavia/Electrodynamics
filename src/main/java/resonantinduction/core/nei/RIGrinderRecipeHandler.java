package resonantinduction.core.nei;

import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import calclavia.lib.utility.LanguageUtility;

public class RIGrinderRecipeHandler extends RITemplateRecipeHandler
{

	@Override
	public String getRecipeName()
	{
		return LanguageUtility.getLocal("resonantinduction.machine.grinder");
	}

	@Override
	public RecipeType getMachine()
	{
		return RecipeType.GRINDER;
	}
}

package resonantinduction.core.nei;

import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import calclavia.lib.utility.LanguageUtility;

public class RICrusherRecipeHandler extends RITemplateRecipeHandler
{

	@Override
	public String getRecipeName()
	{
		return LanguageUtility.getLocal("resonantinduction.machine.crusher");
	}

	@Override
	public RecipeType getMachine()
	{
		return RecipeType.CRUSHER;
	}
}

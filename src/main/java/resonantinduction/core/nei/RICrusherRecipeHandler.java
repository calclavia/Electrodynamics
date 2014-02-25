package resonantinduction.core.nei;

import calclavia.lib.utility.LanguageUtility;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;

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

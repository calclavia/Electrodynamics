package resonantinduction.core.nei;

import calclavia.lib.utility.LanguageUtility;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;

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

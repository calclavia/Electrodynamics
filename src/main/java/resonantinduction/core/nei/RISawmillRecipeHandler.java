package resonantinduction.core.nei;

import calclavia.lib.utility.LanguageUtility;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;

public class RISawmillRecipeHandler extends RITemplateRecipeHandler
{

	@Override
	public String getRecipeName()
	{
		return LanguageUtility.getLocal("resonantinduction.machine.sawmill");
	}

	@Override
	public void loadTransferRects()
	{
		
	}

	@Override
	public RecipeType getMachine()
	{
		return RecipeType.SAWMILL;
	}
}

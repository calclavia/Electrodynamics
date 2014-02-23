package resonantinduction.core.nei;

import calclavia.lib.utility.LanguageUtility;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;

public class RIGrinderRecipeHandler extends RITemplateRecipeHandler
{

	@Override
	public String getRecipeName()
	{
		return LanguageUtility.getLocal("resonantinduction.machine.grinder");
	}

	@Override
	public void loadTransferRects()
	{
		
	}

	@Override
	public RecipeType getMachine()
	{
		return RecipeType.GRINDER;
	}
}

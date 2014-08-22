package resonantinduction.compatibility.nei;

import resonant.lib.utility.LanguageUtility;
import resonant.content.factory.resources.RecipeType;

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

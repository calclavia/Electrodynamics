package resonantinduction.api.recipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import resonantinduction.api.recipe.RecipeUtils.ItemStackResource;
import resonantinduction.api.recipe.RecipeUtils.OreDictResource;
import resonantinduction.api.recipe.RecipeUtils.Resource;

public final class MachineRecipes
{
	public static enum RecipeType
	{
		GRINDER, SAWMILL, SMELTER;
	}

	private final Map<RecipeType, Map<Resource[], Resource[]>> recipes = new HashMap<RecipeType, Map<Resource[], Resource[]>>();

	public static final MachineRecipes INSTANCE = new MachineRecipes();

	private MachineRecipes()
	{
		for (RecipeType machine : RecipeType.values())
		{
			this.recipes.put(machine, new HashMap<Resource[], Resource[]>());
		}
	}

	public void addRecipe(RecipeType machine, Resource[] input, Resource[] output)
	{
		this.recipes.get(machine).put(input, output);
	}

	public void addRecipe(RecipeType machine, ItemStack input, ItemStack output)
	{
		this.addRecipe(machine, new ItemStackResource[] { new ItemStackResource(input) }, new ItemStackResource[] { new ItemStackResource(output) });
	}

	public void addRecipe(RecipeType machine, String input, ItemStack output)
	{
		this.addRecipe(machine, new OreDictResource[] { new OreDictResource(input) }, new ItemStackResource[] { new ItemStackResource(output) });
	}

	public void removeRecipe(RecipeType machine, Resource[] input)
	{
		this.recipes.get(machine).remove(input);
	}

	public Map<Resource[], Resource[]> getRecipes(RecipeType machine)
	{
		return new HashMap<Resource[], Resource[]>(this.recipes.get(machine));
	}

	public Map<RecipeType, Map<Resource[], Resource[]>> getRecipes()
	{
		return new HashMap<RecipeType, Map<Resource[], Resource[]>>(this.recipes);
	}

	public Resource[] getOutput(RecipeType machine, Resource... input)
	{
		Iterator<Entry<Resource[], Resource[]>> it = this.getRecipes(machine).entrySet().iterator();

		while (it.hasNext())
		{
			Entry<Resource[], Resource[]> entry = it.next();

			if (Arrays.equals(entry.getKey(), input))
			{
				return entry.getValue();
			}
		}

		return new Resource[] {};
	}

	public Resource[] getOutput(RecipeType machine, ItemStack... inputs)
	{
		Resource[] resourceInputs = new Resource[inputs.length];

		for (int i = 0; i < inputs.length; i++)
		{
			resourceInputs[i] = new ItemStackResource(inputs[i]);
		}

		return this.getOutput(machine, resourceInputs);
	}

	public Resource[] getOutput(RecipeType machine, String... oreDictNames)
	{
		Resource[] resourceInputs = new Resource[oreDictNames.length];

		for (int i = 0; i < oreDictNames.length; i++)
		{
			resourceInputs[i] = new OreDictResource(oreDictNames[i]);
		}

		return this.getOutput(machine, resourceInputs);
	}
}

package resonantinduction.api.recipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import resonantinduction.api.recipe.RecipeResource.ItemStackResource;
import resonantinduction.api.recipe.RecipeResource.OreDictResource;

public final class MachineRecipes
{
	public static enum RecipeType
	{
		CRUSHER, GRINDER, MIXER, SMELTER, SAWMILL;
	}

	private final Map<RecipeType, Map<RecipeResource[], RecipeResource[]>> recipes = new HashMap<RecipeType, Map<RecipeResource[], RecipeResource[]>>();

	public static final MachineRecipes INSTANCE = new MachineRecipes();

	private MachineRecipes()
	{
		for (RecipeType machine : RecipeType.values())
		{
			this.recipes.put(machine, new HashMap<RecipeResource[], RecipeResource[]>());
		}
	}

	public void addRecipe(RecipeType machine, RecipeResource[] input, RecipeResource[] output)
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

	public void addRecipe(RecipeType machine, String input, String... oreNameOutputs)
	{
		OreDictResource[] outputs = new OreDictResource[oreNameOutputs.length];

		for (int i = 0; i < outputs.length; i++)
		{
			outputs[i] = new OreDictResource(oreNameOutputs[i]);
		}

		addRecipe(machine, new OreDictResource[] { new OreDictResource(input) }, outputs);
	}

	public void addRecipe(RecipeType machine, String input, OreDictResource... output)
	{
		this.addRecipe(machine, new OreDictResource[] { new OreDictResource(input) }, output);
	}

	public void removeRecipe(RecipeType machine, RecipeResource[] input)
	{
		this.recipes.get(machine).remove(input);
	}

	public Map<RecipeResource[], RecipeResource[]> getRecipes(RecipeType machine)
	{
		return new HashMap<RecipeResource[], RecipeResource[]>(this.recipes.get(machine));
	}

	public Map<RecipeType, Map<RecipeResource[], RecipeResource[]>> getRecipes()
	{
		return new HashMap<RecipeType, Map<RecipeResource[], RecipeResource[]>>(this.recipes);
	}

	public RecipeResource[] getOutput(RecipeType machine, RecipeResource... input)
	{
		Iterator<Entry<RecipeResource[], RecipeResource[]>> it = this.getRecipes(machine).entrySet().iterator();

		while (it.hasNext())
		{
			Entry<RecipeResource[], RecipeResource[]> entry = it.next();

			if (Arrays.equals(entry.getKey(), input))
			{
				return entry.getValue();
			}
		}

		return new RecipeResource[] {};
	}

	public RecipeResource[] getOutput(RecipeType machine, ItemStack... inputs)
	{
		RecipeResource[] resourceInputs = new RecipeResource[inputs.length];

		for (int i = 0; i < inputs.length; i++)
		{
			resourceInputs[i] = new ItemStackResource(inputs[i]);
		}

		return this.getOutput(machine, resourceInputs);
	}

	public RecipeResource[] getOutput(RecipeType machine, String... oreDictNames)
	{
		RecipeResource[] resourceInputs = new RecipeResource[oreDictNames.length];

		for (int i = 0; i < oreDictNames.length; i++)
		{
			resourceInputs[i] = new OreDictResource(oreDictNames[i]);
		}

		return this.getOutput(machine, resourceInputs);
	}
}

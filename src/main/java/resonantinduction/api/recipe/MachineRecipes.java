package resonantinduction.api.recipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.api.recipe.RecipeResource.FluidStackResource;
import resonantinduction.api.recipe.RecipeResource.ItemStackResource;
import resonantinduction.api.recipe.RecipeResource.OreDictResource;

public final class MachineRecipes
{
	public static enum RecipeType
	{
		CRUSHER, GRINDER, MIXER, SMELTER, SAWMILL;
	}

	private final Map<RecipeType, Map<RecipeResource[], RecipeResource[]>> recipes = new HashMap<RecipeType, Map<RecipeResource[], RecipeResource[]>>();

	public static MachineRecipes INSTANCE = new MachineRecipes();

	public MachineRecipes()
	{
		for (RecipeType machine : RecipeType.values())
		{
			recipes.put(machine, new HashMap<RecipeResource[], RecipeResource[]>());
		}
	}

	public RecipeResource getResourceFromObject(Object obj)
	{
		if (obj instanceof String)
			return new OreDictResource((String) obj);

		if (obj instanceof Block)
			return new ItemStackResource(new ItemStack((Block) obj));

		if (obj instanceof Item)
			return new ItemStackResource(new ItemStack((Item) obj));

		if (obj instanceof ItemStack)
			return new ItemStackResource((ItemStack) obj);

		if (obj instanceof FluidStack)
			return new FluidStackResource((FluidStack) obj);

		if (obj instanceof RecipeResource)
			return (RecipeResource) obj;

		return null;
	}

	public void addRecipe(RecipeType machine, RecipeResource[] input, RecipeResource[] output)
	{
		this.recipes.get(machine).put(input, output);
	}

	public void addRecipe(RecipeType machine, Object inputObj, Object... outputObj)
	{
		RecipeResource input = getResourceFromObject(inputObj);
		RecipeResource[] outputs = new RecipeResource[outputObj.length];

		for (int i = 0; i < outputs.length; i++)
		{
			RecipeResource output = getResourceFromObject(outputObj[i]);

			if (input == null || output == null)
				throw new RuntimeException("Resonant Induction tried to add invalid machine recipe: " + input + " => " + output);

			outputs[i] = output;
		}

		addRecipe(machine, new RecipeResource[] { input }, outputs);
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

	public RecipeResource[] getOutput(RecipeType machine, Object... inputs)
	{
		RecipeResource[] resourceInputs = new RecipeResource[inputs.length];

		for (int i = 0; i < inputs.length; i++)
		{
			resourceInputs[i] = getResourceFromObject(inputs[i]);
		}

		return getOutput(machine, resourceInputs);
	}
}

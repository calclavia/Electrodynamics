package com.calclavia.edx.core.recipe;

import com.calclavia.edx.core.recipe.RecipeResource.OreDictResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

//TODO: Integrate with NOVA Recipes
public final class MachineRecipes {
	public static MachineRecipes instance = new MachineRecipes();
	private final Map<String, Map<RecipeResource[], RecipeResource[]>> recipes = new HashMap<String, Map<RecipeResource[], RecipeResource[]>>();

	public RecipeResource getResourceFromObject(Object obj) {
		if (obj instanceof String) {
			return new OreDictResource((String) obj);
		}
/*
		if (obj instanceof Block) {
			return new ItemStackResource(new ItemStack((Block) obj));
		}

		if (obj instanceof Item) {
			return new ItemStackResource(new ItemStack((Item) obj));
		}

		if (obj instanceof ItemStack) {
			return new ItemStackResource((ItemStack) obj);
		}

		if (obj instanceof FluidStack) {
			return new FluidStackResource((FluidStack) obj);
		}

		if (obj instanceof RecipeResource) {
			return (RecipeResource) obj;
		}
*/
		return null;
	}

	public void addRecipe(String machine, Object inputObj, Object... outputObj) {
		addRecipe(machine, new Object[] { inputObj }, outputObj);
	}

	public void addRecipe(String machine, Object[] inputObj, Object[] outputObj) {
		RecipeResource[] inputs = new RecipeResource[inputObj.length];

		for (int i = 0; i < inputs.length; i++) {
			RecipeResource input = getResourceFromObject(inputObj[i]);

			if (input == null) {
				throw new RuntimeException("Tried to add invalid " + machine + " recipe input: " + inputObj[i]);
			}

			inputs[i] = input;
		}

		RecipeResource[] outputs = new RecipeResource[outputObj.length];

		for (int i = 0; i < outputs.length; i++) {
			RecipeResource output = getResourceFromObject(outputObj[i]);

			if (output == null) {
				throw new RuntimeException("Tried to add invalid " + machine + " recipe output: " + outputObj[i]);
			}

			outputs[i] = output;
		}

		addRecipe(machine, inputs, outputs);
	}

	public void addRecipe(String machine, RecipeResource[] input, RecipeResource[] output) {
		getRecipes(machine).put(input, output);
	}

	public void removeRecipe(String machine, RecipeResource[] input) {
		getRecipes(machine).remove(input);
	}

	public Map<RecipeResource[], RecipeResource[]> getRecipes(String machine) {
		machine = machine.toLowerCase(Locale.ENGLISH);

		if (!recipes.containsKey(machine)) {
			recipes.put(machine, new HashMap<RecipeResource[], RecipeResource[]>());
		}

		return recipes.get(machine);
	}

	public RecipeResource[] getOutput(String machine, RecipeResource... input) {
		Iterator<Entry<RecipeResource[], RecipeResource[]>> it = getRecipes(machine).entrySet().iterator();

		while (it.hasNext()) {
			Entry<RecipeResource[], RecipeResource[]> entry = it.next();
			RecipeResource[] compare = entry.getKey();

			RecipeResource[] copyA = Arrays.copyOf(input, input.length);
			RecipeResource[] copyB = Arrays.copyOf(compare, compare.length);
			Arrays.sort(copyA);
			Arrays.sort(copyB);
			// TODO: This might not fully work.
			if (Arrays.equals(compare, input) || Arrays.equals(copyA, copyB)) {
				return entry.getValue();
			}
		}

		return new RecipeResource[] {};
	}

	/**
	 * Gets all possible outputs for a specific machine
	 *
	 * @return Will never return null, but an empty array
	 */
	public RecipeResource[] getOutput(String machine, Object... inputs) {
		RecipeResource[] resourceInputs = new RecipeResource[inputs.length];

		for (int i = 0; i < inputs.length; i++) {
			resourceInputs[i] = getResourceFromObject(inputs[i]);
		}

		return getOutput(machine, resourceInputs);
	}
}

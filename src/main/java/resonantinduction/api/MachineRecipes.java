package resonantinduction.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import resonantinduction.api.RecipeUtils.ItemStackResource;
import resonantinduction.api.RecipeUtils.OreDictResource;
import resonantinduction.api.RecipeUtils.Resource;

public final class MachineRecipes
{
	public static enum RecipeType
	{
		GRINDER, SAWMILL, SMELTER, FURNACE, ROLLER, BLAST_FURNACE, METAL_FORMER;
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

	public Resource[] getRecipe(RecipeType machine, ItemStack... inputs)
	{
		return this.getRecipes(machine).get(inputs);
	}

}

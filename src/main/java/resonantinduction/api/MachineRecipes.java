package resonantinduction.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import resonantinduction.api.RecipeUtils.Resource;

public final class MachineRecipes
{
    public static enum RecipeType
    {
        GRINDER,
        SAWMILL,
        SMELTER,
        FURNACE,
        ROLLER,
        BLAST_FURNACE,
        METAL_FORMER;
    }
    
    private final Map<RecipeType, Map<List<Resource>, List<Resource>>> recipes = new HashMap<RecipeType, Map<List<Resource>, List<Resource>>>();
    
    public static final MachineRecipes INSTANCE = new MachineRecipes();
    
    private MachineRecipes()
    {
        for (RecipeType machine : RecipeType.values())
        {
            this.recipes.put(machine, new HashMap<List<Resource>, List<Resource>>());
        }
    }
    
    public void addRecipe(RecipeType machine, List<Resource> input, List<Resource> output)
    {
        this.recipes.get(machine).put(input, output);
    }
    
    public void removeRecipe(RecipeType machine, List<Resource> input)
    {
        this.recipes.get(machine).remove(input);
    }
    
    public Map<List<Resource>, List<Resource>> getRecipes(RecipeType machine)
    {
        return new HashMap<List<Resource>, List<Resource>>(this.recipes.get(machine));
    }
    
    public Map<RecipeType, Map<List<Resource>, List<Resource>>> getRecipes()
    {
        return new HashMap<RecipeType, Map<List<Resource>, List<Resource>>>(this.recipes);
    }

}

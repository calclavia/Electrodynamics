package resonantinduction.core.nei;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.RecipeResource;
import resonantinduction.api.recipe.RecipeResource.FluidStackResource;
import resonantinduction.api.recipe.RecipeResource.ItemStackResource;
import resonantinduction.api.recipe.RecipeResource.OreDictResource;
import resonantinduction.core.Reference;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public abstract class RITemplateRecipeHandler extends TemplateRecipeHandler
{
	int[][] inputSlots = new int[][] { 
		{11,  5}, {29,  5},
		{11, 23}, {29, 23},
		{11, 41}, {29, 41}
	};
	int[][] outputSlots = new int[][] { 
		{121,  5}, {139,  5},
		{121, 23}, {139, 23},
		{121, 41}, {139, 41}
	};

	@Override
	public abstract String getRecipeName();
	
	public abstract MachineRecipes.RecipeType getMachine();

    @Override
    public String getOverlayIdentifier()
    {
        return getMachine().name().toLowerCase();
    }
    
	@Override
	public void loadTransferRects()
	{
		//transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(57, 26, 52, 22), getMachine().name().toLowerCase(), new Object[0]));
		// No point, there is no GUI class to use it... :(
	}

    @Override
    public int recipiesPerPage()
    {
        return 1;
    }
	
	@Override
	public String getGuiTexture()
	{
		return Reference.PREFIX + Reference.GUI_DIRECTORY + "gui_machine.png";
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return null;
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		for (Map.Entry<RecipeResource[], RecipeResource[]> irecipe : MachineRecipes.INSTANCE.getRecipes(getMachine()).entrySet())
		{
			CachedRIRecipe recipe = new CachedRIRecipe(irecipe);
			if (recipe.canProduce(result))
			{
				this.arecipes.add(recipe);
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		for (Map.Entry<RecipeResource[], RecipeResource[]> irecipe : MachineRecipes.INSTANCE.getRecipes(getMachine()).entrySet())
		{
			CachedRIRecipe recipe = new CachedRIRecipe(irecipe);
			if (recipe.doesUse(ingredient))
			{
				this.arecipes.add(recipe);
			}
		}
	}

	public class CachedRIRecipe extends TemplateRecipeHandler.CachedRecipe
	{
		// Raw
		private RecipeResource[] inputResources;
		private RecipeResource[] outputResources;

		// Cache
		private List<PositionedStack> inputs = new ArrayList<PositionedStack>();
		private List<PositionedStack> outputs = new ArrayList<PositionedStack>();

		@Override
		public List<PositionedStack> getOtherStacks()
		{
			if (outputs != null && !outputs.isEmpty())
				return outputs;

			int i = 0;
			outputs = new ArrayList<PositionedStack>();
			for (RecipeResource output : outputResources)
			{
				if (output instanceof ItemStackResource)
				{
					this.outputs.add(new PositionedStack(((ItemStackResource) output).itemStack, outputSlots[i][0], outputSlots[i++][1]));
				} else if (output instanceof OreDictResource)
				{
					this.outputs.add(new PositionedStack(OreDictionary.getOres(((OreDictResource) output).name), outputSlots[i][0], outputSlots[i++][1]));
				} else if (output instanceof FluidStackResource)
				{
					//this.inputs.add(new PositionedStack(((FluidStackResource) output), outputSlots[i][0], outputSlots[i++][1]));
					// TODO fluidstack compatibility
				}
				this.outputs.get(this.outputs.size() - 1).generatePermutations();
			}
			return outputs;
		}

		@Override
		public List<PositionedStack> getIngredients()
		{
			if (inputs != null && !inputs.isEmpty())
				return inputs;

			int i = 0;
			inputs = new ArrayList<PositionedStack>();
			for (RecipeResource input : inputResources)
			{
				if (input instanceof ItemStackResource)
				{
					this.inputs.add(new PositionedStack(((ItemStackResource) input).itemStack, inputSlots[i][0], inputSlots[i++][1]));
				} else if (input instanceof OreDictResource)
				{
					this.inputs.add(new PositionedStack(OreDictionary.getOres(((OreDictResource) input).name), inputSlots[i][0], inputSlots[i++][1]));
				} else if (input instanceof FluidStackResource)
				{
					//this.inputs.add(new PositionedStack(((FluidStackResource) input), inputSlots[i][0], inputSlots[i++][1]));
					// TODO fluidstack compatibility
				}
				this.inputs.get(this.inputs.size() - 1).generatePermutations();
			}
			return inputs;
		}

		public CachedRIRecipe(Map.Entry<RecipeResource[], RecipeResource[]> recipe)
		{

			this.inputResources = recipe.getKey();
			this.outputResources = recipe.getValue();
		}

		@Override
		public PositionedStack getResult()
		{
			return null;
		}

		public boolean canProduce(ItemStack product)
		{
			boolean canProduce = false;
			this.getOtherStacks();
			
			for (int i = 0; i < this.outputResources.length; i ++)
			{
				RecipeResource rStack = this.outputResources[i];
				if (rStack.equals(product))
				{
					this.outputs.get(i).item = product;
					canProduce = true;
				}
			}

			return canProduce;
		}

		public boolean doesUse(ItemStack input)
		{
			boolean doesUse = false;
			this.getIngredients();
			
			for (int i = 0; i < this.inputResources.length; i++)
			{
				RecipeResource rStack = this.inputResources[i];
				if (rStack.equals(input))
				{
					this.inputs.get(i).item = input;
					doesUse = true;
				}
			}

			return doesUse;
		}

	}

}

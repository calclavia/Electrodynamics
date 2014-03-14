package resonantinduction.core.nei;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.Reference;
import calclavia.api.resonantinduction.recipe.MachineRecipes;
import calclavia.api.resonantinduction.recipe.RecipeResource;
import calclavia.api.resonantinduction.recipe.RecipeResource.FluidStackResource;
import calclavia.api.resonantinduction.recipe.RecipeResource.ItemStackResource;
import calclavia.api.resonantinduction.recipe.RecipeResource.OreDictResource;
import calclavia.lib.utility.LanguageUtility;
import codechicken.core.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public abstract class RITemplateRecipeHandler extends TemplateRecipeHandler
{
	int[][] inputSlots = new int[][] { { 11, 5 }, { 29, 5 }, { 11, 23 }, { 29, 23 }, { 11, 41 }, { 29, 41 } };
	int[][] outputSlots = new int[][] { { 121, 5 }, { 139, 5 }, { 121, 23 }, { 139, 23 }, { 121, 41 }, { 139, 41 } };

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
		transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(57, 26, 52, 22), getMachine().name().toLowerCase(), new Object[0]));
	}

	@Override
	public int recipiesPerPage()
	{
		return 2;
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
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if (outputId.equals(this.getOverlayIdentifier()))
		{
			for (Map.Entry<RecipeResource[], RecipeResource[]> irecipe : MachineRecipes.INSTANCE.getRecipes(getMachine()).entrySet())
			{
				CachedRIRecipe recipe = new CachedRIRecipe(irecipe);
				this.arecipes.add(recipe);
			}
		}
		else
		{
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void drawExtras(int recipeID)
	{
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;

		CachedRecipe cachedRecipe = this.arecipes.get(recipeID);

		if (cachedRecipe instanceof CachedRIRecipe)
		{
			CachedRIRecipe recipe = (CachedRIRecipe) cachedRecipe;
			for (int i = 0; i < recipe.inputFluids.length; i++)
			{
				FluidStack fluid = recipe.inputFluids[i];
				if (fluid == null)
					break;

				gui.drawTexturedModelRectFromIcon(inputSlots[i][0], inputSlots[i][1], fluid.getFluid().getIcon(), 16, 16);
			}

			for (int i = 0; i < recipe.outputFluids.length; i++)
			{
				FluidStack fluid = recipe.outputFluids[i];
				if (fluid == null)
					break;

				gui.drawTexturedModelRectFromIcon(outputSlots[i][0], outputSlots[i][1], fluid.getFluid().getIcon(), 16, 16);
			}
		}
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipeID)
	{
		if (currenttip.size() == 0)
		{
			Point offset = gui.getRecipePosition(recipeID);
			Point mouse = GuiDraw.getMousePosition();

			CachedRecipe cachedRecipe = this.arecipes.get(recipeID);

			if (cachedRecipe instanceof CachedRIRecipe)
			{
				CachedRIRecipe recipe = (CachedRIRecipe) cachedRecipe;
				FluidStack fluid;
				Rectangle rect;

				for (int i = 0; i < recipe.inputFluids.length; i++)
				{
					fluid = recipe.inputFluids[i];
					if (fluid == null)
						break;

					rect = new Rectangle(offset.x + inputSlots[i][0] - 1, offset.y + inputSlots[i][1] - 1, 18, 18);
					if (rect.contains(mouse))
					{
						currenttip.add(fluid.getFluid().getLocalizedName());
						currenttip.add(LanguageUtility.getLocal("tooltip.ri.amount").replace("%s", fluid.amount + ""));
						return currenttip;
					}
				}

				for (int i = 0; i < recipe.outputFluids.length; i++)
				{
					fluid = recipe.outputFluids[i];
					if (fluid == null)
						break;

					rect = new Rectangle(offset.x + outputSlots[i][0] - 1, offset.y + outputSlots[i][1] - 1, 18, 18);
					if (rect.contains(mouse))
					{
						currenttip.add(fluid.getFluid().getLocalizedName());
						currenttip.add(LanguageUtility.getLocal("tooltip.ri.amount").replace("%s", fluid.amount + ""));
						return currenttip;
					}
				}
			}
		}
		return currenttip;
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
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		if (inputId.equals(this.getOverlayIdentifier()))
		{
			for (Map.Entry<RecipeResource[], RecipeResource[]> irecipe : MachineRecipes.INSTANCE.getRecipes(getMachine()).entrySet())
			{
				CachedRIRecipe recipe = new CachedRIRecipe(irecipe);
				this.arecipes.add(recipe);
			}
		}
		else
		{
			super.loadUsageRecipes(inputId, ingredients);
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

		final FluidStack[] inputFluids = new FluidStack[inputSlots.length];
		final FluidStack[] outputFluids = new FluidStack[outputSlots.length];

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
				}
				else if (output instanceof OreDictResource)
				{
					this.outputs.add(new PositionedStack(OreDictionary.getOres(((OreDictResource) output).name), outputSlots[i][0], outputSlots[i++][1]));
				}
				else if (output instanceof FluidStackResource)
				{
					this.outputFluids[i++] = ((FluidStackResource) output).fluidStack;
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
				}
				else if (input instanceof OreDictResource)
				{
					this.inputs.add(new PositionedStack(OreDictionary.getOres(((OreDictResource) input).name), inputSlots[i][0], inputSlots[i++][1]));
				}
				else if (input instanceof FluidStackResource)
				{
					this.inputFluids[i++] = ((FluidStackResource) input).fluidStack;
				}

				if (this.inputs.size() > 0)
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

			for (int i = 0; i < this.outputResources.length; i++)
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

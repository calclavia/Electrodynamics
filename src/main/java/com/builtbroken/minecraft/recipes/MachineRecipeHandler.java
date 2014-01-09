package com.builtbroken.minecraft.recipes;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import resonantinduction.api.IProcessable;
import calclavia.lib.utility.AutoCraftingManager;

import com.builtbroken.common.Pair;

/**
 * Recipes for ore processor machines
 * 
 * @author DarkGuardsman
 */
public class MachineRecipeHandler
{
	private static Random random = new Random();

	static
	{
		newProcessorRecipe(ProcessorType.CRUSHER, Block.stone, Block.cobblestone);
		newProcessorRecipe(ProcessorType.CRUSHER, Block.oreDiamond, Item.diamond);
		newProcessorRecipe(ProcessorType.CRUSHER, Block.oreLapis, new ItemStack(Item.dyePowder.itemID, 4, 4));
		newProcessorRecipe(ProcessorType.CRUSHER, Block.oreRedstone, new ItemStack(Item.redstone.itemID, 4, 0));
		newProcessorRecipe(ProcessorType.CRUSHER, Block.oreEmerald, new ItemStack(Item.redstone.itemID, 4, 0));

		newProcessorRecipe(ProcessorType.GRINDER, new ItemStack(Block.cobblestone.blockID, 1, 0), new ItemStack(Block.sand.blockID, 1, 0));
		newProcessorRecipe(ProcessorType.GRINDER, Block.glass, Block.sand);

		MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, Block.stone, Block.cobblestone);
		MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, Block.cobblestoneMossy, Block.cobblestone);
		MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, Block.glass, Block.sand);
		MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, Item.stick, null);

		MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, Block.stone, Block.cobblestone);
		MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, Block.cobblestoneMossy, Block.cobblestone);
		MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, Item.stick, null);

		// TODO replace these with ItemOreDirv glass shards
		MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, Block.glass, Block.sand);
	}

	/**
	 * Creates a new recipe for the type of processor machine
	 * 
	 * @param type - machine type
	 * @param in - input item, stacksize is ignored
	 * @param out - output item
	 */
	public static void newProcessorRecipe(ProcessorType type, Object in, Object out)
	{
		newProcessorRecipe(type, in, out, -1, -1);
	}

	/**
	 * Creates a new recipe for the type of processor machine
	 * 
	 * @param type - machine type
	 * @param in - input item, stacksize is ignored
	 * @param out - output item
	 * @param min - min stacksize to return as output
	 * @param max- max stacksize to return as output
	 */
	public static void newProcessorRecipe(ProcessorType type, Object in, Object out, int min, int max)
	{
		newProcessorRecipe(type, in, out, min, max, false);
	}

	/**
	 * Creates a new recipe for the type of processor machine
	 * 
	 * @param type - machine type
	 * @param in - input item, stacksize is ignored
	 * @param out - output item
	 * @param min - min stacksize to return as output
	 * @param max- max stacksize to return as output
	 * @param ignoreNBT - only use this if your item's nbt doesn't play a factor in what items were
	 * used to craft it
	 */
	public static void newProcessorRecipe(ProcessorType type, Object in, Object out, int min, int max, boolean ignoreNBT)
	{
		if (in != null && out != null && type != null)
		{
			ItemStack input = convertToItemStack(in);
			ItemStack output = convertToItemStack(out);
			if (input != null && output != null && type.recipes != null)
			{
				if (!ignoreNBT && (input.getTagCompound() != null || input.isItemEnchanted()))
				{
					System.out.println("[MachineRecipeHandler]Error: NBT or Enchanted Items must use the IProccesable interface to properlly handle recipe outputs.");
					System.out.println("[MachineRecipeHandler]Item>>  Data:  " + input.toString() + "    Name:  " + input.getItem().getUnlocalizedName());
					return;
				}
				if (min == -1)
				{
					min = output.stackSize;
				}
				if (max == -1 || max < min)
				{
					max = output.stackSize;
				}
				type.recipes.put(new Pair<Integer, Integer>(input.itemID, input.getItemDamage()), new ProcessorRecipe(output, min, max));
			}
		}
	}

	/** Used to track items that should be converted to different items during salvaging. */
	public static void newAltProcessorOutput(ProcessorType type, Object in, Object out)
	{
		if (in != null && out != null && type != null)
		{
			ItemStack input = convertToItemStack(in);
			ItemStack output = convertToItemStack(out);
			if (input != null && output != null && type.altOutput != null)
			{
				type.altOutput.put(new Pair<Integer, Integer>(input.itemID, input.getItemDamage()), output);
			}
		}
	}

	/** Marks an itemstack as unsalvagable by all processors */
	public static void banProcessingOfItem(ItemStack stack)
	{
		if (stack != null)
		{
			for (ProcessorType type : ProcessorType.values())
			{
				banProcessingOfItem(type, stack);
			}
		}
	}

	/**
	 * Marks an itemstack as unusable by processors. This will jam the processor if the item enters
	 * it
	 */
	public static void banProcessingOfItem(ProcessorType type, ItemStack stack)
	{
		if (type != null && stack != null)
		{
			type.banList.add(new Pair<Integer, Integer>(stack.itemID, stack.getItemDamage()));
		}
	}

	/** Converts an object input into an itemstack for use */
	private static ItemStack convertToItemStack(Object object)
	{
		if (object instanceof ItemStack)
		{
			ItemStack stack = (ItemStack) object;
			if (stack.getItemDamage() < 0)
			{
				stack.setItemDamage(0);
			}
			return stack;
		}
		if (object instanceof Block)
		{
			return new ItemStack(((Block) object).blockID, 1, -1);
		}
		if (object instanceof Item)
		{
			return new ItemStack(((Item) object).itemID, 1, -1);
		}
		return null;
	}

	/**
	 * Gets the lit of items that are created from the input item stack. General this will be an
	 * array of one item. However, in salavaging cases it can be up to 8 items.
	 * 
	 * @param type - Processor type
	 * @param inputStack - item stack input ignores stacksize
	 * @return array of itemStacks
	 */
	public static ItemStack[] getProcessorOutput(ProcessorType type, ItemStack inputStack)
	{
		if (inputStack != null && type != null)
		{
			ItemStack[] reList = null;
			if (inputStack.getItem() instanceof IProcessable)
			{
				if (!((IProcessable) inputStack.getItem()).canProcess(type, inputStack))
				{
					return null;
				}
				reList = ((IProcessable) inputStack.getItem()).getProcesserOutput(type, inputStack);
			}
			if (reList == null)
			{
				reList = getOuputNormal(type, inputStack);
			}
			if (reList == null)
			{
				// TODO Disabled due to bug and needs to be fixed to make the processors more
				// functional
				// reList = salvageItem(type, inputStack);
			}
			return reList;
		}
		return null;
	}

	/**
	 * Salvages an itemStack for the items used to craft it
	 * 
	 * @param type - processor type used to determine damage results
	 * @param stack - itemStack being salvaged
	 * @return Array of all items salvaged
	 */
	public static ItemStack[] salvageItem(ProcessorType type, ItemStack stack)
	{
		return salvageItem(type, stack, true);
	}

	/**
	 * Salvages an itemStack for the items used to craft it
	 * 
	 * @param type - processor type used to determine damage results
	 * @param stack - itemStack being salvaged
	 * @param damage - damage the output items. Eg ironIngot becomes ironDust, or ironScraps
	 * @return Array of all items salvaged
	 */
	public static ItemStack[] salvageItem(ProcessorType type, ItemStack stack, boolean damage)
	{
		float bar = 0.1f;
		// Allow tools and armor to be salvaged but at a very low rate
		if ((stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemTool) && stack.isItemDamaged())
		{
			bar = (stack.getItemDamage() / stack.getMaxDamage());
		}
		ItemStack[] reList = salvageItem(stack, bar);
		if (damage && reList != null && type.altOutput != null)
		{
			for (int i = 0; i < reList.length; i++)
			{
				if (type.altOutput.containsKey(new Pair<Integer, Integer>(reList[i].itemID, reList[i].getItemDamage())))
				{
					reList[i] = convertToItemStack(type.altOutput.get(new Pair<Integer, Integer>(reList[i].itemID, reList[i].getItemDamage())));
				}
			}
		}
		return reList;
	}

	/**
	 * Salvages an itemStack for the items used to craft it
	 * 
	 * @param stack - itemStack being salvaged
	 * @param bar - chance per item that the random must be above inorder to salvage the output
	 * @return Array of all items salvaged
	 */
	public static ItemStack[] salvageItem(ItemStack stack, float bar)
	{
		// TODO find a way around having to force recipe to be the same stack size of the salvage.
		// Maybe percentage based salvaging or min stacksize from machine?
		ItemStack[] recipeList = AutoCraftingManager.getReverseRecipe(stack.copy(), stack.stackSize);
		if (recipeList != null)
		{
			ItemStack[] reList = new ItemStack[recipeList.length];
			boolean items = false;
			for (int i = 0; i < recipeList.length; i++)
			{
				if (random.nextFloat() >= bar)
				{
					reList[i] = recipeList[i].copy();
					items = true;
					if (recipeList[i].itemID < Block.blocksList.length && Block.blocksList[recipeList[i].itemID] != null && recipeList[i].getItemDamage() > 16)
					{
						reList[i].setItemDamage(0);
					}

				}
			}
			return items ? reList : null;
		}
		return null;
	}

	public static ItemStack[] getOuputNormal(ProcessorType type, ItemStack stack)
	{
		if (type.recipes != null)
		{
			ProcessorRecipe re = type.recipes.get(new Pair<Integer, Integer>(stack.itemID, -1));
			if (re == null || re.output == null)
			{
				re = type.recipes.get(new Pair<Integer, Integer>(stack.itemID, stack.getItemDamage()));
			}
			if (type.altOutput != null && (re == null || re.output == null))
			{
				return new ItemStack[] { type.altOutput.get(new Pair<Integer, Integer>(stack.itemID, stack.getItemDamage())) };
			}
			if (re != null && re.output != null)
			{
				ItemStack output = re.output.copy();
				output.stackSize = Math.min(re.maxItemsOut, re.minItemsOut + random.nextInt(re.minItemsOut));
				if (re.chancePerItem < 1.0f)
				{

				}
				return new ItemStack[] { output };
			}
		}

		return null;
	}

}

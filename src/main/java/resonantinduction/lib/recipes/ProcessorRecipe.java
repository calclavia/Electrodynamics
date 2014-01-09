package resonantinduction.lib.recipes;

import net.minecraft.item.ItemStack;

/** Processor Recipe output Container. Input is controlled by the processor recipes class. */
public class ProcessorRecipe
{
	/** Output of the recipe */
	public ItemStack output;
	/** Chance per item after the stack size has been calculated from min and max size */
	public float chancePerItem = 1.0f;
	/** Min the recipe can output */
	public int minItemsOut = -1;
	/** Max the recipe can output */
	public int maxItemsOut = -1;

	public ProcessorRecipe(ItemStack output, int min, int max)
	{
		this.output = output;
		this.minItemsOut = min;
		this.maxItemsOut = max;
	}
}

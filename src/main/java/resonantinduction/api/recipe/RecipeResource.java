package resonantinduction.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public abstract class RecipeResource
{
	public final boolean hasChance;
	public final float chance;

	protected RecipeResource()
	{
		this.hasChance = false;
		this.chance = 100;
	}

	protected RecipeResource(float chance)
	{
		this.hasChance = true;
		this.chance = chance;
	}

	public boolean hasChance()
	{
		return this.hasChance;
	}

	public float getChance()
	{
		return this.chance;
	}

	public abstract ItemStack getItemStack();

	public static class ItemStackResource extends RecipeResource
	{
		public final ItemStack itemStack;

		public ItemStackResource(ItemStack is)
		{
			super();
			this.itemStack = is;
		}

		public ItemStackResource(ItemStack is, float chance)
		{
			super(chance);
			this.itemStack = is;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof ItemStackResource)
			{
				return this.itemStack.isItemEqual(((ItemStackResource) obj).itemStack);
			}
			if (obj instanceof ItemStack)
			{
				return this.itemStack.isItemEqual((ItemStack) obj);
			}

			return false;
		}

		@Override
		public ItemStack getItemStack()
		{
			return itemStack.copy();
		}

		@Override
		public String toString()
		{
			return "[ItemStackResource: " + itemStack.toString() + "]";
		}
	}

	public static class OreDictResource extends RecipeResource
	{
		public final String name;

		public OreDictResource(String s)
		{
			super();
			this.name = s;

			if (OreDictionary.getOres(name).size() <= 0)
			{
				throw new RuntimeException("Added invalid OreDictResource recipe: " + name);
			}
		}

		public OreDictResource(String s, float chance)
		{
			super(chance);
			this.name = s;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof OreDictResource)
			{
				return this.name.equals(((OreDictResource) obj).name);
			}

			if (obj instanceof ItemStackResource)
			{
				for (ItemStack is : OreDictionary.getOres(name).toArray(new ItemStack[0]))
					if (is.isItemEqual(((ItemStackResource) obj).itemStack))
						return true;
			}

			if (obj instanceof ItemStack)
			{
				for (ItemStack is : OreDictionary.getOres(name).toArray(new ItemStack[0]))
					if (is.isItemEqual((ItemStack) obj))
						return true;
				return false;
			}

			return false;
		}

		@Override
		public ItemStack getItemStack()
		{
			return OreDictionary.getOres(name).get(0).copy();
		}

		@Override
		public String toString()
		{
			return "[OreDictResource: " + name + "]";
		}
	}

	public static class FluidStackResource extends RecipeResource
	{
		public final FluidStack fluidStack;

		public FluidStackResource(FluidStack fs)
		{
			super();
			this.fluidStack = fs;
		}

		public FluidStackResource(FluidStack fs, float chance)
		{
			super(chance);
			this.fluidStack = fs;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof FluidStackResource)
				return equals(((FluidStackResource) obj).fluidStack);

			return (obj instanceof FluidStack) ? ((FluidStack) obj).equals(obj) : false;
		}

		@Override
		public ItemStack getItemStack()
		{
			return null;
		}

		@Override
		public String toString()
		{
			return "[FluidStackResource: " + fluidStack.getFluid().getName() + "]";
		}
	}
}

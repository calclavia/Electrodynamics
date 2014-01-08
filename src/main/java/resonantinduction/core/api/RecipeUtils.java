package resonantinduction.core.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeUtils
{
	public static abstract class Resource
	{
		public final boolean hasChance;
		public final float chance;

		protected Resource()
		{
			this.hasChance = false;
			this.chance = 100;
		}

		protected Resource(float chance)
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
	}

	public static class ItemStackResource extends Resource
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

			return false;
		}
	}

	public static class OreDictResource extends Resource
	{
		public final String name;

		public OreDictResource(String s)
		{
			super();
			this.name = s;
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
				return this.name.equals(OreDictionary.getOreName(OreDictionary.getOreID(((ItemStackResource) obj).itemStack)));
			}

			return false;
		}
	}

	public static class FluidStackResource extends Resource
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
			return (obj instanceof FluidStack) ? ((FluidStack) obj).equals(obj) : false;
		}
	}
}

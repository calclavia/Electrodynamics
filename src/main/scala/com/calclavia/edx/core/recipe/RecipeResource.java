package com.calclavia.edx.core.recipe;

import nova.core.fluid.Fluid;
import nova.core.item.Item;

public abstract class RecipeResource {
	public final boolean hasChance;
	public final float chance;

	protected RecipeResource() {
		this.hasChance = false;
		this.chance = 100;
	}

	protected RecipeResource(float chance) {
		this.hasChance = true;
		this.chance = chance;
	}

	public boolean hasChance() {
		return this.hasChance;
	}

	public float getChance() {
		return this.chance;
	}

	public abstract Item getItem();

	public static class ItemResource extends RecipeResource {
		public final Item item;

		public ItemResource(Item is) {
			super();
			this.item = is;
		}

		public ItemResource(Item is, float chance) {
			super(chance);
			this.item = is;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ItemResource) {
				return this.item.sameType(((ItemResource) obj).item);
			}
			if (obj instanceof Item) {
				return this.item.sameType((Item) obj);
			}
			return false;
		}

		@Override
		public Item getItem() {
			return item.clone();
		}

		@Override
		public String toString() {
			return "[ItemResource: " + item.toString() + "]";
		}
	}

	public static class OreDictResource extends RecipeResource {
		public final String name;

		public OreDictResource(String s) {
			super();
			this.name = s;
/*
			if (OreDictionary.getOres(name).size() <= 0) {
				throw new RuntimeException("Added invalid OreDictResource recipe: " + name);
			}*/
		}

		public OreDictResource(String s, float chance) {
			super(chance);
			this.name = s;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof OreDictResource) {
				return name.equals(((OreDictResource) obj).name);
			}

			if (obj instanceof ItemResource) {
				return equals(((ItemResource) obj).item);
			}

			if (obj instanceof Item) {
			/*	for (Item is : OreDictionary.getOres(name)) {
					if (is.sameType((Item) obj)) {
						return true;
					}
				}*/
			}

			return false;
		}

		@Override
		public Item getItem() {
			return null;//OreDictionary.getOres(name).get(0).copy();
		}

		@Override
		public String toString() {
			return "[OreDictResource: " + name + "]";
		}
	}

	public static class FluidResource extends RecipeResource {
		public final Fluid fluid;

		public FluidResource(Fluid fs) {
			super();
			this.fluid = fs;
		}

		public FluidResource(Fluid fs, float chance) {
			super(chance);
			this.fluid = fs;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FluidResource) {
				return equals(((FluidResource) obj).fluid);
			}

			return (obj instanceof Fluid) ? ((Fluid) obj).equals(fluid) : false;
		}

		@Override
		public Item getItem() {
			return null;
		}

		@Override
		public String toString() {
			return "[FluidResource: " + fluid.getID() + "]";
		}
	}
}

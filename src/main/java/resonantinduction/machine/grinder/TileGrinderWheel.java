package resonantinduction.machine.grinder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import resonantinduction.api.MachineRecipes;
import resonantinduction.api.MachineRecipes.RecipeType;
import resonantinduction.api.RecipeUtils.ItemStackResource;
import resonantinduction.api.RecipeUtils.Resource;
import calclavia.lib.prefab.tile.TileElectrical;

/**
 * @author Calclavia
 * 
 */
public class TileGrinderWheel extends TileElectrical
{
	/** A map of ItemStacks and their remaining grind-time left. */
	public static final HashMap<EntityItem, Integer> map = new HashMap<EntityItem, Integer>();

	public void doWork()
	{
		Iterator<Entry<EntityItem, Integer>> it = map.entrySet().iterator();

		while (it.hasNext())
		{
			Entry<EntityItem, Integer> entry = it.next();
			entry.setValue(entry.getValue() - 1);

			if (entry.getValue() <= 0)
			{
				this.doGrind(entry.getKey());
			}
		}
	}

	private boolean canGrind(ItemStack itemStack)
	{
		return MachineRecipes.INSTANCE.getRecipes(RecipeType.GRINDER).containsKey(itemStack);
	}

	private void doGrind(EntityItem entity)
	{
		ItemStack itemStack = entity.getEntityItem();

		List<Resource> results = MachineRecipes.INSTANCE.getRecipes(RecipeType.GRINDER).get(itemStack);

		for (Resource resource : results)
		{
			if (resource instanceof ItemStackResource)
			{
				entity.setEntityItemStack(((ItemStackResource) resource).itemStack);
			}
		}
	}
}

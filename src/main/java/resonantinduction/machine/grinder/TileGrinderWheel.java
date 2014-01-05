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
import universalelectricity.api.energy.EnergyStorageHandler;
import calclavia.lib.prefab.tile.TileElectrical;

/**
 * @author Calclavia
 * 
 */
public class TileGrinderWheel extends TileElectrical
{
	/** A map of ItemStacks and their remaining grind-time left. */
	public final HashMap<EntityItem, Integer> grinderTimer = new HashMap<EntityItem, Integer>();

	public TileGrinderWheel()
	{
		this.energy = new EnergyStorageHandler(100000);
	}

	@Override
	public void updateEntity()
	{
		// TODO: Add electricity support.
		doWork();
	}

	public void doWork()
	{
		Iterator<Entry<EntityItem, Integer>> it = grinderTimer.entrySet().iterator();

		while (it.hasNext())
		{
			Entry<EntityItem, Integer> entry = it.next();
			entry.setValue(entry.getValue() - 1);

			if (entry.getValue() <= 0)
			{
				this.doGrind(entry.getKey());
			}
			else
			{
				// Make the entity not be able to be picked up.
				EntityItem entity = entry.getKey();
				entity.delayBeforeCanPickup = 20;
				this.worldObj.spawnParticle("smoke", entity.posX, entity.posY, entity.posZ, 0, 0, 0);
			}
		}
	}

	public boolean canGrind(ItemStack itemStack)
	{
		return MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack) == null ? false : MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack).length > 0;
	}

	private void doGrind(EntityItem entity)
	{
		ItemStack itemStack = entity.getEntityItem();

		Resource[] results = MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack);

		for (Resource resource : results)
		{
			if (resource instanceof ItemStackResource)
			{
				entity.setEntityItemStack(((ItemStackResource) resource).itemStack);
				entity.posY -= 1;
			}
		}
	}
}

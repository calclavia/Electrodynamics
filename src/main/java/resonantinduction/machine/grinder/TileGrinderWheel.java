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
	public static final HashMap<EntityItem, Integer> grinderTimer = new HashMap<EntityItem, Integer>();

	public EntityItem grindingItem = null;

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
		if (grindingItem != null && grinderTimer.containsKey(grindingItem))
		{
			int timeLeft = grinderTimer.get(grindingItem) - 1;
			grinderTimer.put(grindingItem, timeLeft);

			if (timeLeft <= 0)
			{
				if (this.doGrind(grindingItem))
				{
					grindingItem.setDead();
					grinderTimer.remove(grindingItem);
					grindingItem = null;
				}
			}
			else
			{
				grindingItem.delayBeforeCanPickup = 20;
				this.worldObj.spawnParticle("crit", grindingItem.posX, grindingItem.posY, grindingItem.posZ, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3);
			}
		}
	}

	public boolean canGrind(ItemStack itemStack)
	{
		return MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack) == null ? false : MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack).length > 0;
	}

	private boolean doGrind(EntityItem entity)
	{
		if (!this.worldObj.isRemote)
		{
			ItemStack itemStack = entity.getEntityItem();

			Resource[] results = MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack);

			for (Resource resource : results)
			{
				if (resource instanceof ItemStackResource)
				{
					EntityItem entityItem = new EntityItem(this.worldObj, entity.posX, entity.posY, entity.posZ, ((ItemStackResource) resource).itemStack.copy());
					entityItem.delayBeforeCanPickup = 20;
					entityItem.motionX = 0;
					entityItem.motionY = 0;
					entityItem.motionZ = 0;
					this.worldObj.spawnEntityInWorld(entityItem);
				}
			}
		}

		return true;
	}
}

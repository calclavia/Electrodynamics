package mffs.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.modules.IModule;
import mffs.api.modules.IModuleAcceptor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidContainerRegistry;

public abstract class TileEntityModuleAcceptor extends TileEntityFortron implements IModuleAcceptor
{
	/**
	 * Caching for the module stack data. This is used to reduce calculation time. Cache gets reset
	 * when inventory changes.
	 */
	public final HashMap<String, Object> cache = new HashMap<String, Object>();

	public int startModuleIndex = 0;
	public int endModuleIndex = this.getSizeInventory() - 1;

	@Override
	public ItemStack getModule(IModule module)
	{
		String cacheID = "getModule_" + module.hashCode();

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof ItemStack)
				{
					return (ItemStack) this.cache.get(cacheID);
				}
			}
		}

		ItemStack returnStack = new ItemStack((Item) module, 0);

		for (ItemStack comparedModule : getModuleStacks())
		{
			if (comparedModule.getItem() == module)
			{
				returnStack.stackSize += comparedModule.stackSize;
			}
		}

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, returnStack.copy());
		}

		return returnStack;
	}

	@Override
	public int getModuleCount(IModule module, int... slots)
	{
		int count = 0;

		if (module != null)
		{
			String cacheID = "getModuleCount_" + module.hashCode();

			if (slots != null)
			{
				cacheID += "_" + Arrays.hashCode(slots);
			}

			if (Settings.USE_CACHE)
			{
				if (this.cache.containsKey(cacheID))
				{
					if (this.cache.get(cacheID) instanceof Integer)
					{
						return (Integer) this.cache.get(cacheID);
					}
				}
			}

			if (slots != null && slots.length > 0)
			{
				for (int slotID : slots)
				{
					if (this.getStackInSlot(slotID) != null)
					{
						if (this.getStackInSlot(slotID).getItem() == module)
						{
							count += this.getStackInSlot(slotID).stackSize;
						}
					}
				}
			}
			else
			{
				for (ItemStack itemStack : getModuleStacks())
				{
					if (itemStack.getItem() == module)
					{
						count += itemStack.stackSize;
					}
				}
			}

			if (Settings.USE_CACHE)
			{
				this.cache.put(cacheID, count);
			}
		}

		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ItemStack> getModuleStacks(int... slots)
	{
		String cacheID = "getModuleStacks_";

		if (slots != null)
		{
			cacheID += Arrays.hashCode(slots);
		}

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof Set)
				{
					return (Set<ItemStack>) this.cache.get(cacheID);
				}
			}
		}

		Set<ItemStack> modules = new HashSet<ItemStack>();

		if (slots == null || slots.length <= 0)
		{
			for (int slotID = startModuleIndex; slotID <= endModuleIndex; slotID++)
			{
				ItemStack itemStack = this.getStackInSlot(slotID);

				if (itemStack != null)
				{
					if (itemStack.getItem() instanceof IModule)
					{
						modules.add(itemStack);
					}
				}
			}
		}
		else
		{
			for (int slotID : slots)
			{
				ItemStack itemStack = this.getStackInSlot(slotID);

				if (itemStack != null)
				{
					if (itemStack.getItem() instanceof IModule)
					{
						modules.add(itemStack);
					}
				}
			}
		}

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, modules);
		}

		return modules;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<IModule> getModules(int... slots)
	{
		String cacheID = "getModules_";

		if (slots != null)
		{
			cacheID += Arrays.hashCode(slots);
		}

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof Set)
				{
					return (Set<IModule>) this.cache.get(cacheID);
				}
			}
		}

		Set<IModule> modules = new HashSet<IModule>();

		if (slots == null || slots.length <= 0)
		{
			for (int slotID = startModuleIndex; slotID <= endModuleIndex; slotID++)
			{
				ItemStack itemStack = this.getStackInSlot(slotID);

				if (itemStack != null)
				{
					if (itemStack.getItem() instanceof IModule)
					{
						modules.add((IModule) itemStack.getItem());
					}
				}
			}
		}
		else
		{
			for (int slotID : slots)
			{
				ItemStack itemStack = this.getStackInSlot(slotID);

				if (itemStack != null)
				{
					if (itemStack.getItem() instanceof IModule)
					{
						modules.add((IModule) itemStack.getItem());
					}
				}
			}
		}

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, modules);
		}

		return modules;
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		this.fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.itemModuleCapacity) * 10 + 500) * LiquidContainerRegistry.BUCKET_VOLUME);

		/**
		 * Clears the cache.
		 */
		this.cache.clear();
	}
}

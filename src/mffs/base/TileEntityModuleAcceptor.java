package mffs.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mffs.api.modules.IModule;
import mffs.api.modules.IModuleAcceptor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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

		if (this.cache.containsKey(cacheID))
		{
			if (this.cache.get(cacheID) instanceof ItemStack)
			{
				return (ItemStack) this.cache.get(cacheID);
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

		this.cache.put(cacheID, returnStack.copy());

		return returnStack;
	}

	@Override
	public int getModuleCount(IModule module, int... slots)
	{
		String cacheID = "getModuleCount_" + module.hashCode() + "_" + slots.hashCode();

		if (this.cache.containsKey(cacheID))
		{
			if (this.cache.get(cacheID) instanceof Integer)
			{
				return (int) this.cache.get(cacheID);
			}
		}

		int count = 0;

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

		this.cache.put(cacheID, count);

		return count;
	}

	@Override
	public Set<ItemStack> getModuleStacks(int... slots)
	{
		String cacheID = "getModuleStacks_" + slots.hashCode();

		if (this.cache.containsKey(cacheID))
		{
			if (this.cache.get(cacheID) instanceof Set)
			{
				return (Set<ItemStack>) this.cache.get(cacheID);
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

		this.cache.put(cacheID, modules);
		return modules;
	}

	@Override
	public Set<IModule> getModules(int... slots)
	{
		String cacheID = "getModules_" + slots.hashCode();

		if (this.cache.containsKey(cacheID))
		{
			if (this.cache.get(cacheID) instanceof Set)
			{
				return (Set<IModule>) this.cache.get(cacheID);
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

		this.cache.put(cacheID, modules);

		return modules;
	}

	public void onInventoryChanged()
	{
		/**
		 * Clears the cache.
		 */
		this.cache.clear();
	}
}

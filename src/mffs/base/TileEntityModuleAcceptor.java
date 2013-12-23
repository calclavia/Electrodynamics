package mffs.base;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.ICache;
import mffs.api.modules.IModule;
import mffs.api.modules.IModuleAcceptor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileEntityModuleAcceptor extends TileEntityFortron implements IModuleAcceptor, ICache
{
	/**
	 * Caching for the module stack data. This is used to reduce calculation time. Cache gets reset
	 * when inventory changes.
	 */
	public final HashMap<String, Object> cache = new HashMap<String, Object>();

	public int startModuleIndex = 0;
	public int endModuleIndex = this.getSizeInventory() - 1;

	protected int capacityBase = 500;
	protected int capacityBoost = 5;

	/** Used for client-side only. */
	public int clientFortronCost = 0;

	@Override
	public List getPacketUpdate()
	{
		List objects = super.getPacketUpdate();
		objects.add(this.getFortronCost());
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (this.worldObj.isRemote)
		{
			if (packetID == TilePacketType.DESCRIPTION.ordinal())
			{
				this.clientFortronCost = dataStream.readInt();
			}
		}
	}

	@Override
	public void initiate()
	{
		super.initiate();
		this.fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.itemModuleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME);
	}

	public void consumeCost()
	{
		if (this.getFortronCost() > 0)
		{
			this.requestFortron(this.getFortronCost(), true);
		}
	}

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

	/**
	 * Returns Fortron cost in ticks.
	 */
	@Override
	public final int getFortronCost()
	{
		if (this.worldObj.isRemote)
		{
			return this.clientFortronCost;
		}

		String cacheID = "getFortronCost";

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				Object obj = this.cache.get(cacheID);

				if (obj != null && obj instanceof Integer)
				{
					return (Integer) obj;
				}
			}
		}

		int result = this.doGetFortronCost();

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, result);
		}

		return this.doGetFortronCost();
	}

	protected int doGetFortronCost()
	{
		float cost = 0;

		for (ItemStack itemStack : this.getModuleStacks())
		{
			if (itemStack != null)
			{
				cost += itemStack.stackSize * ((IModule) itemStack.getItem()).getFortronCost(this.getAmplifier());
			}
		}

		return Math.round(cost);
	}

	protected float getAmplifier()
	{
		return 1;
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		this.fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.itemModuleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME);

		/**
		 * Clears the cache.
		 */
		this.clearCache();
	}

	@Override
	public Object getCache(String cacheID)
	{
		return this.cache.get(cacheID);
	}

	@Override
	public void clearCache(String cacheID)
	{
		this.cache.remove(cacheID);
	}

	@Override
	public void clearCache()
	{
		this.cache.clear();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.clientFortronCost = nbt.getInteger("fortronCost");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("fortronCost", this.clientFortronCost);
	}
}

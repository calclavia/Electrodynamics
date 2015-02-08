package mffs.api.modules;

import net.minecraft.item.ItemStack;

import java.util.Set;

public interface IModuleProvider {
	/**
	 * Gets the ItemStack of a specific module type. This ItemStack is constructed and NOT a reference to the actual stacks within the block.
	 */
	public ItemStack getModule(IModule module);

	public int getModuleCount(IModule module, int... slots);

	public Set<ItemStack> getModuleStacks(int... slots);

	public Set<IModule> getModules(int... slots);

	public int getFortronCost();
}

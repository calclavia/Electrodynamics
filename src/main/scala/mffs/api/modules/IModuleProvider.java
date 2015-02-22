package mffs.api.modules;

import net.minecraft.item.Item;

import java.util.Set;

public interface IModuleProvider {
	/**
	 * Gets the Item of a specific module type. This Item is constructed and NOT a reference to the actual stacks within the block.
	 */
	public Item getModule(Module module);

	public int getModuleCount(Module module, int... slots);

	public Set<Item> getModuleStacks(int... slots);

	public Set<Module> getModules(int... slots);

	public int getFortronCost();
}

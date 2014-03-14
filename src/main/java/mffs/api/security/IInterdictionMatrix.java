package mffs.api.security;

import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import calclavia.api.mffs.IActivatable;
import calclavia.api.mffs.IBiometricIdentifierLink;
import calclavia.api.mffs.fortron.IFortronFrequency;
import calclavia.api.mffs.modules.IModuleAcceptor;

public interface IInterdictionMatrix extends IInventory, IFortronFrequency, IModuleAcceptor, IBiometricIdentifierLink, IActivatable
{

	/**
	 * The range in which the Interdiction Matrix starts warning the player.
	 * 
	 * @return
	 */
	public int getWarningRange();

	/**
	 * The range in which the Interdiction Matrix has an effect on.
	 * 
	 * @return
	 */
	public int getActionRange();

	/**
	 * Merges an item into the Interdiction Matrix's safe keeping inventory.
	 * 
	 * @param itemStack
	 * @return True if kept, false if dropped.
	 */
	public boolean mergeIntoInventory(ItemStack itemStack);

	public Set<ItemStack> getFilteredItems();

	/**
	 * 
	 * @return True if the filtering is on ban mode. False if it is on allow-only mode.
	 */
	public boolean getFilterMode();

	@Override
	public int getFortronCost();
}

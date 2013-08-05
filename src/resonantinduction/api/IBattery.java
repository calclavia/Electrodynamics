/**
 * 
 */
package resonantinduction.api;

import net.minecraft.item.ItemStack;

/**
 * TODO: Use UE interface after ModJAm
 * 
 * @author Calclavia
 * 
 */
public interface IBattery
{
	public float getEnergyStored(ItemStack itemStack);

	public float getMaxEnergyStored(ItemStack itemStack);

	/**
	 * @param itemStack
	 * @param amount
	 */
	public void setEnergyStored(ItemStack itemStack, float amount);
}

/**
 * 
 */
package resonantinduction.battery;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import resonantinduction.api.IBattery;
import resonantinduction.base.ItemBase;

/**
 * Stores power.
 * 
 * @author Calclavia
 * 
 */
public class ItemCapacitor extends ItemBase implements IBattery
{
	public ItemCapacitor(int id)
	{
		super("capacitor", id);
		this.setMaxStackSize(1);
		this.setMaxDamage(1000);
	}

	@Override
	public void setEnergyStored(ItemStack itemStack, float amount)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		itemStack.getTagCompound().setFloat("energyStored", amount);
	}

	@Override
	public float getEnergyStored(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		return itemStack.getTagCompound().getFloat("energyStored");
	}

	@Override
	public float getMaxEnergyStored()
	{
		return 100;
	}
}

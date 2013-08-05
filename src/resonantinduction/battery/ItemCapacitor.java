/**
 * 
 */
package resonantinduction.battery;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
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
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		double energyStored = this.getEnergyStored(itemStack);
		par3List.add("Energy: " + energyStored + " J");
	}

	@Override
	public void setEnergyStored(ItemStack itemStack, float amount)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		itemStack.getTagCompound().setFloat("energyStored", amount);
		itemStack.setItemDamage((int) (amount / this.getMaxEnergyStored()));
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

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		ItemStack chargedStack = new ItemStack(par1, 1, 0);
		this.setEnergyStored(chargedStack, this.getMaxEnergyStored());
		par3List.add(chargedStack);
		ItemStack unchargedStack = new ItemStack(par1, 1, 0);
		this.setEnergyStored(unchargedStack, 0);
		par3List.add(unchargedStack);
	}

}

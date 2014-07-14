package resonantinduction.electrical.battery;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import resonant.lib.render.EnumColor;
import resonant.lib.utility.LanguageUtility;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.item.IEnergyItem;
import universalelectricity.api.item.IVoltageItem;

@UniversalClass
public class ItemBlockBattery extends ItemBlock implements IEnergyItem, IVoltageItem
{
	public ItemBlockBattery(int id)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
		this.setNoRepair();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
	{
		list.add(LanguageUtility.getLocal("tooltip.tier") + ": " + (getTier(itemStack) + 1));

		String color = "";
		long joules = this.getEnergy(itemStack);

		if (joules <= this.getEnergyCapacity(itemStack) / 3)
		{
			color = "\u00a74";
		}
		else if (joules > this.getEnergyCapacity(itemStack) * 2 / 3)
		{
			color = "\u00a72";
		}
		else
		{
			color = "\u00a76";
		}
		itemStack.getItemDamageForDisplay();
		list.add(LanguageUtility.getLocal("tooltip.battery.energy").replace("%0", color).replace("%1", EnumColor.GREY.toString()).replace("%v0", UnitDisplay.getDisplayShort(joules, Unit.JOULES)).replace("%v1", UnitDisplay.getDisplayShort(this.getEnergyCapacity(itemStack), Unit.JOULES)));
	}

	/**
	 * Makes sure the item is uncharged when it is crafted and not charged. Change this if you do
	 * not want this to happen!
	 */
	@Override
	public void onCreated(ItemStack itemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		this.setEnergy(itemStack, 0);
	}

	@Override
	public long recharge(ItemStack itemStack, long energy, boolean doReceive)
	{
		long rejectedElectricity = Math.max((this.getEnergy(itemStack) + energy) - this.getEnergyCapacity(itemStack), 0);
		long energyToReceive = Math.min(energy - rejectedElectricity, getTransferRate(itemStack));

		if (doReceive)
		{
			this.setEnergy(itemStack, this.getEnergy(itemStack) + energyToReceive);
		}

		return energyToReceive;
	}

	@Override
	public long discharge(ItemStack itemStack, long energy, boolean doTransfer)
	{
		long energyToExtract = Math.min(Math.min(this.getEnergy(itemStack), energy), getTransferRate(itemStack));

		if (doTransfer)
		{
			this.setEnergy(itemStack, this.getEnergy(itemStack) - energyToExtract);
		}

		return energyToExtract;
	}

	@Override
	public long getVoltage(ItemStack itemStack)
	{
		return UniversalElectricity.DEFAULT_VOLTAGE;
	}

	@Override
	public void setEnergy(ItemStack itemStack, long joules)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		long electricityStored = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0);
		itemStack.getTagCompound().setLong("electricity", electricityStored);
	}

	public long getTransfer(ItemStack itemStack)
	{
		return this.getEnergyCapacity(itemStack) - this.getEnergy(itemStack);
	}

	/** Gets the energy stored in the item. Energy is stored using item NBT */
	@Override
	public long getEnergy(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		long energyStored = itemStack.getTagCompound().getLong("electricity");
		return energyStored;
	}

	public static ItemStack setTier(ItemStack itemStack, byte tier)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setByte("tier", tier);
		return itemStack;
	}

	public static byte getTier(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		return itemStack.getTagCompound().getByte("tier");
	}

	@Override
	public int getDisplayDamage(ItemStack stack)
	{
		return (int) (100 - ((double) this.getEnergy(stack) / (double) getEnergyCapacity(stack)) * 100);
	}

	@Override
	public long getEnergyCapacity(ItemStack theItem)
	{
		return TileBattery.getEnergyForTier(getTier(theItem));
	}

	public long getTransferRate(ItemStack itemStack)
	{
		return this.getEnergyCapacity(itemStack) / 100;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (byte tier = 0; tier <= TileBattery.MAX_TIER; tier++)
		{
			par3List.add(CompatibilityModule.getItemWithCharge(setTier(new ItemStack(this), tier), 0));
			par3List.add(CompatibilityModule.getItemWithCharge(setTier(new ItemStack(this), tier), TileBattery.getEnergyForTier(tier)));
		}
	}
}

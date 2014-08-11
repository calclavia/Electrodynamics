package resonantinduction.electrical.battery;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import resonant.lib.render.EnumColor;
import resonant.lib.utility.LanguageUtility;
import universalelectricity.api.UnitDisplay;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.item.IEnergyItem;
import universalelectricity.compatibility.Compatibility;

@UniversalClass
public class ItemBlockBattery extends ItemBlock implements IEnergyItem
{
	public ItemBlockBattery(Block block)
	{
		super(block);
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
		double joules = this.getEnergy(itemStack);

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
		list.add(LanguageUtility.getLocal("tooltip.battery.energy").replace("%0", color).replace("%1", EnumColor.GREY.toString()).replace("%v0", new UnitDisplay(UnitDisplay.Unit.JOULES, joules).toString()).replace("%v1", new UnitDisplay(UnitDisplay.Unit.JOULES, this.getEnergyCapacity(itemStack),  true).toString()));
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
	public double recharge(ItemStack itemStack, double energy, boolean doReceive)
	{
		double rejectedElectricity = Math.max((this.getEnergy(itemStack) + energy) - this.getEnergyCapacity(itemStack), 0);
		double energyToReceive = Math.min(energy - rejectedElectricity, getTransferRate(itemStack));

		if (doReceive)
		{
			this.setEnergy(itemStack, this.getEnergy(itemStack) + energyToReceive);
		}

		return energyToReceive;
	}

	@Override
	public double discharge(ItemStack itemStack, double energy, boolean doTransfer)
	{
		double energyToExtract = Math.min(Math.min(this.getEnergy(itemStack), energy), getTransferRate(itemStack));

		if (doTransfer)
		{
			this.setEnergy(itemStack, this.getEnergy(itemStack) - energyToExtract);
		}

		return energyToExtract;
	}

	@Override
	public double getVoltage(ItemStack itemStack)
	{
		return 240;
	}

	@Override
	public void setEnergy(ItemStack itemStack, double joules)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0);
		itemStack.getTagCompound().setDouble("electricity", electricityStored);
	}

	public double getTransfer(ItemStack itemStack)
	{
		return this.getEnergyCapacity(itemStack) - this.getEnergy(itemStack);
	}

	/** Gets the energy stored in the item. Energy is stored using item NBT */
	@Override
	public double getEnergy(ItemStack itemStack)
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
	public double getEnergyCapacity(ItemStack theItem)
	{
		return TileBattery.getEnergyForTier(getTier(theItem));
	}

	public double getTransferRate(ItemStack itemStack)
	{
		return this.getEnergyCapacity(itemStack) / 100;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (byte tier = 0; tier <= TileBattery.MAX_TIER; tier++)
		{
			par3List.add(Compatibility.getItemWithCharge(setTier(new ItemStack(this), tier), 0));
			par3List.add(Compatibility.getItemWithCharge(setTier(new ItemStack(this), tier), TileBattery.getEnergyForTier(tier)));
		}
	}
}

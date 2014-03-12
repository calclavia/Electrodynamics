package resonantinduction.core.prefab.imprint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import calclavia.lib.utility.nbt.NBTUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;

public class ItemImprint extends Item
{
	public ItemImprint(int id)
	{
		super(Settings.CONFIGURATION.getItem("imprint", id).getInt());
		this.setUnlocalizedName(Reference.PREFIX + "imprint");
		this.setTextureName(Reference.PREFIX + "imprint");
		this.setCreativeTab(TabRI.DEFAULT);
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
	{
		if (entity != null && !(entity instanceof IProjectile) && !(entity instanceof EntityPlayer))
		{
			String stringName = EntityList.getEntityString(entity);
			// TODO Add to filter
			// player.sendChatToPlayer("Target: " + stringName);
			return true;
		}
		return false;
	}

	public boolean itemInteractionForEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLiving)
	{
		return false;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
	{
		Set<ItemStack> filterItems = getFilters(itemStack);

		if (filterItems.size() > 0)
		{
			for (ItemStack filterItem : filterItems)
			{
				list.add(filterItem.getDisplayName());
			}
		}
		else
		{
			list.add("No filters");
		}
	}

	/** Saves the list of items to filter out inside. */
	public static void setFilters(ItemStack itemStack, Set<ItemStack> filterStacks)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		NBTTagList nbt = new NBTTagList();

		for (ItemStack filterStack : filterStacks)
		{
			NBTTagCompound newCompound = new NBTTagCompound();
			filterStack.writeToNBT(newCompound);
			nbt.appendTag(newCompound);
		}

		itemStack.getTagCompound().setTag("Items", nbt);
	}

	public static boolean isFiltering(ItemStack filter, ItemStack itemStack)
	{
		if (filter != null && itemStack != null)
		{
			Set<ItemStack> checkStacks = getFilters(filter);

			if (checkStacks != null)
			{
				for (ItemStack stack : checkStacks)
				{
					if (stack.isItemEqual(itemStack))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	public static HashSet<ItemStack> getFilters(ItemStack itemStack)
	{
		HashSet<ItemStack> filterStacks = new HashSet<ItemStack>();

		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
		NBTTagList tagList = nbt.getTagList("Items");

		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound var4 = (NBTTagCompound) tagList.tagAt(i);
			filterStacks.add(ItemStack.loadItemStackFromNBT(var4));
		}

		return filterStacks;
	}

	public static List<ItemStack> getFilterList(ItemStack itemStack)
	{
		List<ItemStack> filterStacks = new ArrayList<ItemStack>();

		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
		NBTTagList tagList = nbt.getTagList("Items");

		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound var4 = (NBTTagCompound) tagList.tagAt(i);
			filterStacks.add(ItemStack.loadItemStackFromNBT(var4));
		}

		return filterStacks;
	}
}

package resonantinduction.archaic.imprint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.texture.IconRegister;
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
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockImprint extends Item
{
	public ItemBlockImprint()
	{
		this(Settings.getNextItemID());
	}

	public ItemBlockImprint(int id)
	{
		super(Settings.CONFIGURATION.getItem("imprint", id).getInt());
		this.setUnlocalizedName("imprint");
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setHasSubtypes(true);
		this.maxStackSize = 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon(Reference.PREFIX + "imprint");
	}

	@Override
	public int getItemStackLimit()
	{
		return 1;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
	{
		if (entity != null && !(entity instanceof IProjectile) && !(entity instanceof EntityPlayer))
		{
			String stringName = EntityList.getEntityString(entity);
			// TODO add to filter
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

	public static HashSet<ItemStack> getFilters(ItemStack itemStack)
	{
		HashSet<ItemStack> filterStacks = new HashSet<ItemStack>();

		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		NBTTagCompound nbt = itemStack.getTagCompound();
		NBTTagList tagList = nbt.getTagList("Items");

		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound var4 = (NBTTagCompound) tagList.tagAt(i);
			filterStacks.add(ItemStack.loadItemStackFromNBT(var4));
		}

		return filterStacks;
	}
}

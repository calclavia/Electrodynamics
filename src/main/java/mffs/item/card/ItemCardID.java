package mffs.item.card;

import java.util.List;

import mffs.api.card.ICardIdentification;
import mffs.api.security.Permission;
import mffs.card.ItemCard;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.nbt.NBTUtility;

public class ItemCardID extends ItemCard implements ICardIdentification
{
	private static final String NBT_PREFIX = "mffs_permission_";

	public ItemCardID(int i)
	{
		super(i, "cardIdentification");
	}

	public ItemCardID(int i, String name)
	{
		super(i, name);
	}

	@Override
	public boolean hitEntity(ItemStack itemStack, EntityLivingBase entityLiving, EntityLivingBase par3EntityLiving)
	{
		if (entityLiving instanceof EntityPlayer)
		{
			this.setUsername(itemStack, ((EntityPlayer) entityLiving).username);
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		if (this.getUsername(itemStack) != null && !this.getUsername(itemStack).isEmpty())
		{
			info.add(LanguageUtility.getLocal("info.cardIdentification.username") + " " + this.getUsername(itemStack));
		}
		else
		{
			info.add(LanguageUtility.getLocal("info.cardIdentification.empty"));
		}

		String tooltip = "";
		boolean isFirst = true;

		for (Permission permission : Permission.getPermissions())
		{
			if (this.hasPermission(itemStack, permission))
			{
				if (!isFirst)
				{
					tooltip += ", ";
				}

				isFirst = false;
				tooltip += LanguageUtility.getLocal("gui." + permission.name + ".name");
			}
		}
		if (tooltip != null && tooltip.length() > 0)
		{
			info.addAll(LanguageUtility.splitStringPerWord(tooltip, 5));
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World par2World, EntityPlayer entityPlayer)
	{
		this.setUsername(itemStack, entityPlayer.username);
		return itemStack;
	}

	@Override
	public void setUsername(ItemStack itemStack, String username)
	{
		NBTTagCompound nbtTagCompound = NBTUtility.getNBTTagCompound(itemStack);
		nbtTagCompound.setString("name", username);
	}

	@Override
	public String getUsername(ItemStack itemStack)
	{
		NBTTagCompound nbtTagCompound = NBTUtility.getNBTTagCompound(itemStack);

		if (nbtTagCompound != null)
		{
			if (nbtTagCompound.getString("name") != "")
			{
				return nbtTagCompound.getString("name");
			}
		}

		return null;
	}

	@Override
	public boolean hasPermission(ItemStack itemStack, Permission permission)
	{
		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
		return nbt.getBoolean(NBT_PREFIX + permission.id);
	}

	@Override
	public boolean addPermission(ItemStack itemStack, Permission permission)
	{
		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
		nbt.setBoolean(NBT_PREFIX + permission.id, true);
		return false;
	}

	@Override
	public boolean removePermission(ItemStack itemStack, Permission permission)
	{
		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
		nbt.setBoolean(NBT_PREFIX + permission.id, false);
		return false;
	}
}
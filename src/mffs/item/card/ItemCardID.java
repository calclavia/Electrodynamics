package mffs.item.card;

import java.util.List;

import mffs.MFFSHelper;
import mffs.api.card.ICardIdentification;
import mffs.api.security.Permission;
import mffs.card.ItemCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import universalelectricity.prefab.TranslationHelper;

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
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		if (this.getUsername(itemStack) != null)
		{
			info.add("Username: " + this.getUsername(itemStack));
		}
		else
		{
			info.add("Unspecified");
		}

		String tooltip = "";

		for (Permission permission : Permission.getPermissions())
		{
			if (this.hasPermission(itemStack, permission))
			{
				if (permission != Permission.getPermissions()[0])
				{
					tooltip += ", ";
				}

				tooltip += "\u00a72" + TranslationHelper.getLocal("gui." + permission.name + ".name");
			}
		}
		if (tooltip != null && tooltip.length() > 0)
		{
			info.addAll(MFFSHelper.splitStringPerWord(tooltip, 5));
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
		NBTTagCompound nbtTagCompound = MFFSHelper.getNBTTagCompound(itemStack);
		nbtTagCompound.setString("name", username);
	}

	@Override
	public String getUsername(ItemStack itemStack)
	{
		NBTTagCompound nbtTagCompound = MFFSHelper.getNBTTagCompound(itemStack);

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
		NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);
		return nbt.getBoolean(NBT_PREFIX + permission.id);
	}

	@Override
	public boolean addPermission(ItemStack itemStack, Permission permission)
	{
		NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);
		nbt.setBoolean(NBT_PREFIX + permission.id, true);
		return false;
	}

	@Override
	public boolean removePermission(ItemStack itemStack, Permission permission)
	{
		NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);
		nbt.setBoolean(NBT_PREFIX + permission.id, false);
		return false;
	}
}
package mffs.item.card;

import icbm.api.IBlockFrequency;
import icbm.api.IItemFrequency;

import java.util.List;

import mffs.Settings;
import mffs.card.ItemCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import calclavia.lib.utility.LanguageUtility;

public class ItemCardFrequency extends ItemCard implements IItemFrequency
{
	public ItemCardFrequency(String name, int i)
	{
		super(i, name);
	}

	public ItemCardFrequency(int i)
	{
		this("cardFrequency", i);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
	{
		list.add(LanguageUtility.getLocal("info.cardFrequency.freq") + " " + this.getFrequency(itemStack));
	}

	@Override
	public int getFrequency(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			return itemStack.getTagCompound().getInteger("frequency");
		}

		return 0;
	}

	@Override
	public void setFrequency(int frequency, ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.getTagCompound().setInteger("frequency", frequency);
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		if (!world.isRemote)
		{
			if (player.isSneaking())
			{
				this.setFrequency(world.rand.nextInt((int) Math.pow(10, (Settings.MAX_FREQUENCY_DIGITS - 1))), itemStack);
				player.addChatMessage(LanguageUtility.getLocal("message.cardFrequency.generated") + " " + this.getFrequency(itemStack));
			}
		}

		return itemStack;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IBlockFrequency)
		{
			if (!world.isRemote)
			{
				((IBlockFrequency) tileEntity).setFrequency(this.getFrequency(itemStack));
				world.markBlockForUpdate(x, y, z);
				player.addChatMessage(LanguageUtility.getLocal("message.cardFrequency.set").replaceAll("%p", "" + this.getFrequency(itemStack)));
			}

			return true;
		}

		return false;
	}
}
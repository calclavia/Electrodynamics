package mffs.item.card;

import icbm.api.IItemFrequency;

import java.util.List;

import mffs.Settings;
import mffs.base.TileEntityFrequency;
import mffs.card.ItemCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
		list.add("Frequency: " + this.getFrequency(itemStack));
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
				this.setFrequency(world.rand.nextInt(10 ^ Settings.MAX_FREQUENCY_DIGITS - 1), itemStack);
				player.addChatMessage("Generated random frequency: " + this.getFrequency(itemStack));
			}
		}

		return itemStack;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityFrequency)
		{
			if (!world.isRemote)
			{
				((TileEntityFrequency) tileEntity).setFrequency(this.getFrequency(itemStack));
				world.markBlockForUpdate(x, y, z);
				player.addChatMessage("Frequency set to: " + this.getFrequency(itemStack));
			}

			return true;
		}

		return false;
	}
}
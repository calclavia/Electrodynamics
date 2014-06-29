package mffs.item.card;

import resonant.api.mffs.card.ICoordLink;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.card.ItemCard;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import resonant.lib.utility.LanguageUtility;
import universalelectricity.api.vector.VectorWorld;

import java.util.List;

/**
 * A linking card used to link machines in specific positions.
 *
 * @author Calclavia
 */
public class ItemCardLink extends ItemCard implements ICoordLink
{
	public ItemCardLink(int id)
	{
		super(id, "cardLink");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		if (hasLink(itemstack))
		{
			VectorWorld vec = getLink(itemstack);
			int blockId = vec.getBlockID(entityplayer.worldObj);

			if (Block.blocksList[blockId] != null)
			{
				list.add(LanguageUtility.getLocal("info.item.linkedWith") + " " + Block.blocksList[blockId].getLocalizedName());
			}

			list.add(vec.xi() + ", " + vec.yi() + ", " + vec.zi());
			list.add(LanguageUtility.getLocal("info.item.dimension") + " " + vec.world.provider.getDimensionName());
		}
		else
		{
			list.add(LanguageUtility.getLocal("info.item.notLinked"));
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!world.isRemote)
		{
			VectorWorld vector = new VectorWorld(world, x, y, z);
			this.setLink(itemStack, vector);

			if (Block.blocksList[vector.getBlockID(world)] != null)
			{
				player.addChatMessage(LanguageUtility.getLocal("info.item.linkedWith") + " " + x + ", " + y + ", " + z + " - " + Block.blocksList[vector.getBlockID(world)].getLocalizedName());
			}
		}

		return true;
	}

	public boolean hasLink(ItemStack itemStack)
	{
		return getLink(itemStack) != null;
	}

	@Override
	public VectorWorld getLink(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("link"))
		{
			return null;
		}

		return new VectorWorld(itemStack.getTagCompound().getCompoundTag("link"));
	}

	@Override
	public void setLink(ItemStack itemStack, VectorWorld vec)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setCompoundTag("link", vec.writeToNBT(new NBTTagCompound()));
	}

	public void clearLink(ItemStack itemStack)
	{
		itemStack.getTagCompound().removeTag("link");
	}
}
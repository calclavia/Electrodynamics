/**
 * 
 */
package resonantinduction.entangler;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import resonantinduction.base.ItemBase;
import resonantinduction.base.Vector3;

/**
 * @author Calclavia
 * 
 */
public abstract class ItemCoordLink extends ItemBase
{
	public ItemCoordLink(String name, int id)
	{
		super(name, id);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		if (hasLink(itemstack))
		{
			Vector3 vec = getLink(itemstack);
			int dimID = getLinkDim(itemstack);

			list.add("Bound to [" + (int) vec.x + ", " + (int) vec.y + ", " + (int) vec.z + "], dimension '" + dimID + "'");
		}
		else
		{
			list.add("No block bound");
		}
	}

	public boolean hasLink(ItemStack itemStack)
	{
		return getLink(itemStack) != null;
	}

	public Vector3 getLink(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null)
		{
			return null;
		}

		int x = itemStack.stackTagCompound.getInteger("bindX");
		int y = itemStack.stackTagCompound.getInteger("bindY");
		int z = itemStack.stackTagCompound.getInteger("bindZ");

		return new Vector3(x, y, z);
	}

	public void setLink(ItemStack itemStack, Vector3 vec, int dimID)
	{
		if (itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setInteger("bindX", (int) vec.x);
		itemStack.stackTagCompound.setInteger("bindY", (int) vec.y);
		itemStack.stackTagCompound.setInteger("bindZ", (int) vec.z);

		itemStack.stackTagCompound.setInteger("dimID", dimID);
	}

	public int getLinkDim(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null)
		{
			return 0;
		}

		return itemStack.stackTagCompound.getInteger("dimID");
	}
}

/**
 * 
 */
package resonantinduction.entangler;

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

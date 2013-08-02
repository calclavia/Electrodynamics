package resonantinduction.entangler;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import resonantinduction.base.ItemBase;
import resonantinduction.base.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author AidanBrady
 * 
 */
public class ItemQuantumEntangler extends ItemBase
{
	public static int WILDCARD = 1337; /* :) */

	public ItemQuantumEntangler(int id)
	{
		super("entangler", id);
		setMaxStackSize(1);
		// TODO Handheld model, render animation, energy usage (should be easy?)
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		if (hasBind(itemstack))
		{
			Vector3 vec = getBindVec(itemstack);
			int dimID = getDimID(itemstack);

			list.add("Bound to [" + (int) vec.x + ", " + (int) vec.y + ", " + (int) vec.z + "], dimension '" + dimID + "'");
		}
		else
		{
			list.add("No block bound");
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float posX, float posY, float posZ)
	{
		if (!world.isRemote)
		{
			if (world.isAirBlock(x, y + 1, z) && world.isAirBlock(x, y + 2, z))
			{
				int dimID = world.provider.dimensionId;

				entityplayer.addChatMessage("Bound Entangler to block [" + x + ", " + y + ", " + z + "], dimension '" + dimID + "'");
				setBindVec(itemstack, new Vector3(x, y, z), dimID);

				return true;
			}

			entityplayer.addChatMessage("Error: invalid block for binding!");
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if (!world.isRemote)
		{
			if (!hasBind(itemstack))
			{
				entityplayer.addChatMessage("Error: no block bound to Entangler!");
				return itemstack;
			}

			// TELEPORT//

			Vector3 vec = getBindVec(itemstack);
			int dimID = getDimID(itemstack);

			// travel to dimension if different dimID
			if (world.provider.dimensionId != dimID)
			{
				((EntityPlayerMP) entityplayer).travelToDimension(dimID);
			}

			// actually teleport to new coords
			((EntityPlayerMP) entityplayer).playerNetServerHandler.setPlayerLocation(vec.x + 0.5, vec.y + 1, vec.z + 0.5, entityplayer.rotationYaw, entityplayer.rotationPitch);

			world.playSoundAtEntity(entityplayer, "mob.endermen.portal", 1.0F, 1.0F);
		}

		return itemstack;
	}

	public boolean hasBind(ItemStack itemStack)
	{
		return getBindVec(itemStack) != null;
	}

	public Vector3 getBindVec(ItemStack itemStack)
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

	public void setBindVec(ItemStack itemStack, Vector3 vec, int dimID)
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

	public int getDimID(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null)
		{
			return WILDCARD;
		}

		return itemStack.stackTagCompound.getInteger("dimID");
	}
}

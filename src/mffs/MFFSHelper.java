package mffs;

import icbm.api.IBlockFrequency;

import java.util.ArrayList;
import java.util.List;

import mffs.api.IProjector;
import mffs.api.security.IInterdictionMatrix;
import mffs.api.security.Permission;
import mffs.fortron.FrequencyGrid;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

/**
 * A class containing some general helpful functions.
 * 
 * @author Calclavia
 * 
 */
public class MFFSHelper
{
	/**
	 * Gets the nearest active Interdiction Matrix.
	 */
	public static IInterdictionMatrix getNearestInterdictionMatrix(World world, Vector3 position)
	{
		for (IBlockFrequency frequencyTile : FrequencyGrid.instance().get())
		{
			if (((TileEntity) frequencyTile).worldObj == world && frequencyTile instanceof IInterdictionMatrix)
			{
				IInterdictionMatrix interdictionMatrix = (IInterdictionMatrix) frequencyTile;

				if (interdictionMatrix.isActive())
				{
					if (position.distanceTo(new Vector3((TileEntity) interdictionMatrix)) <= interdictionMatrix.getActionRange())
					{
						return interdictionMatrix;
					}
				}
			}
		}

		return null;
	}

	public static boolean isPermittedByInterdictionMatrix(IInterdictionMatrix interdictionMatrix, String username, Permission... permissions)
	{
		if (interdictionMatrix != null)
		{
			if (interdictionMatrix.isActive())
			{
				if (interdictionMatrix.getBiometricIdentifier() != null)
				{
					for (Permission permission : permissions)
					{
						if (!interdictionMatrix.getBiometricIdentifier().isAccessGranted(username, permission))
						{
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	public static List<String> splitStringPerWord(String string, int wordsPerLine)
	{
		String[] words = string.split(" ");
		List<String> lines = new ArrayList<String>();

		for (int lineCount = 0; lineCount < Math.ceil((float) words.length / (float) wordsPerLine); lineCount++)
		{
			String stringInLine = "";

			for (int i = lineCount * wordsPerLine; i < Math.min(wordsPerLine + lineCount * wordsPerLine, words.length); i++)
			{
				stringInLine += words[i] + " ";
			}

			lines.add(stringInLine.trim());
		}

		return lines;
	}

	/**
	 * Gets the first itemStack that is an ItemBlock in this TileEntity or in nearby chests.
	 * 
	 * @param itemStack
	 * @return
	 */
	public static ItemStack getFirstItemBlock(TileEntity tileEntity, ItemStack itemStack)
	{
		return getFirstItemBlock(tileEntity, itemStack, true);
	}

	public static ItemStack getFirstItemBlock(TileEntity tileEntity, ItemStack itemStack, boolean recur)
	{
		if (tileEntity instanceof IProjector)
		{
			for (int i : ((IProjector) tileEntity).getModuleSlots())
			{
				ItemStack checkStack = getFirstItemBlock(i, ((IProjector) tileEntity), itemStack);

				if (checkStack != null)
				{
					return checkStack;
				}
			}
		}
		else if (tileEntity instanceof IInventory)
		{
			IInventory inventory = (IInventory) tileEntity;

			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				ItemStack checkStack = getFirstItemBlock(i, inventory, itemStack);

				if (checkStack != null)
				{
					return checkStack;
				}
			}
		}

		if (recur)
		{
			for (int i = 0; i < 6; i++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				Vector3 vector = new Vector3(tileEntity);
				vector.modifyPositionFromSide(direction);
				TileEntity checkTile = vector.getTileEntity(tileEntity.worldObj);

				if (checkTile != null)
				{
					ItemStack checkStack = getFirstItemBlock(checkTile, itemStack, false);

					if (checkStack != null)
					{
						return checkStack;
					}
				}
			}
		}

		return null;
	}

	public static ItemStack getFirstItemBlock(int i, IInventory inventory, ItemStack itemStack)
	{
		ItemStack checkStack = inventory.getStackInSlot(i);

		if (checkStack != null && checkStack.getItem() instanceof ItemBlock)
		{
			if (itemStack == null || checkStack.isItemEqual(itemStack))
			{
				return checkStack;
			}
		}
		return null;
	}

	public static Block getCamoBlock(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof ItemBlock)
			{
				if (((ItemBlock) itemStack.getItem()).getBlockID() < Block.blocksList.length)
				{
					Block block = Block.blocksList[((ItemBlock) itemStack.getItem()).getBlockID()];

					if (block.renderAsNormalBlock())
					{
						return block;
					}
				}
			}
		}

		return null;

	}

	/**
	 * Gets a compound from an itemStack.
	 * 
	 * @param itemStack
	 * @return
	 */
	public static NBTTagCompound getNBTTagCompound(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			return itemStack.getTagCompound();
		}

		return null;
	}
}

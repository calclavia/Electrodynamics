package mffs;

import java.util.ArrayList;
import java.util.List;

import mffs.api.fortron.IFortronFrequency;
import mffs.api.security.IInterdictionMatrix;
import mffs.fortron.FortronGrid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

/**
 * A class containing some general helpful functions.
 * 
 * @author Calclavia
 * 
 */
public class MFFSHelper
{
	public static IInterdictionMatrix getNearestInterdictionMatrix(World world, Vector3 position)
	{
		for (IFortronFrequency frequencyTile : FortronGrid.instance().get())
		{
			if (((TileEntity) frequencyTile).worldObj == world && frequencyTile instanceof IInterdictionMatrix)
			{
				IInterdictionMatrix interdictionMatrix = (IInterdictionMatrix) frequencyTile;

				if (position.distanceTo(new Vector3((TileEntity) interdictionMatrix)) <= interdictionMatrix.getActionRange())
				{
					return interdictionMatrix;
				}
			}
		}

		return null;
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

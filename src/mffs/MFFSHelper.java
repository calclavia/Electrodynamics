package mffs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * A class containing some general helpful functions.
 * 
 * @author Calclavia
 * 
 */
public class MFFSHelper
{
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

package mffs.fortron;

import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

/**
 * A class with useful functions related to Fortron.
 * 
 * @author Calclavia
 * 
 */
public class FortronHelper
{
	public static LiquidStack LIQUID_FORTRON;

	public static LiquidStack getFortron(int amount)
	{
		LiquidStack stack = FortronHelper.LIQUID_FORTRON.copy();
		stack.amount = amount;
		return stack;
	}

	public static int getAmount(LiquidStack liquidStack)
	{
		if (liquidStack != null)
		{
			return liquidStack.amount;
		}
		return 0;
	}

	public static int getAmount(LiquidTank fortronTank)
	{
		if (fortronTank != null)
		{
			return getAmount(fortronTank.getLiquid());
		}

		return 0;
	}
}

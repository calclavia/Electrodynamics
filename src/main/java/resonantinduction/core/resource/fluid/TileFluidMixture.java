package resonantinduction.core.resource.fluid;

import java.util.TreeSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import calclavia.lib.prefab.tile.TileAdvanced;

/**
 * @author Calclavia
 * 
 */
public class TileFluidMixture extends TileAdvanced
{
	public final TreeSet<FluidStack> fluids = new TreeSet<FluidStack>();

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public void mix(FluidStack fluid)
	{
		if (fluids.contains(fluid))
		{
			for (FluidStack checkFluid : fluids)
			{
				if (fluid.equals(checkFluid))
				{
					checkFluid.amount += fluid.amount;
				}
			}
		}
		else
		{
			fluids.add(fluid);
		}
	}

	/**
	 * @return The color of the liquid based on the fluidStacks stored.
	 */
	public int getColor()
	{
		return 0xFFFFFF;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		readFluidFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		writeFluidToNBT(nbt);
	}

	public void readFluidFromNBT(NBTTagCompound nbt)
	{
		fluids.clear();

		NBTTagList nbtList = nbt.getTagList("fluids");

		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound fluidNBT = (NBTTagCompound) nbtList.tagAt(i);
			fluids.add(FluidStack.loadFluidStackFromNBT(fluidNBT));
		}
	}

	public void writeFluidToNBT(NBTTagCompound nbt)
	{
		NBTTagList nbtList = new NBTTagList();

		for (FluidStack fluid : fluids)
		{
			NBTTagCompound nbtElement = new NBTTagCompound();
			fluid.writeToNBT(nbtElement);
			nbtList.appendTag(nbtElement);
		}

		nbt.setTag("fluids", nbtList);
	}
}

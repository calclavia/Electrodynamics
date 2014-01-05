package resonantinduction.machine.liquid;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
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
	public final Set<FluidStack> fluids = new HashSet<FluidStack>();

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		fluids.clear();

		NBTTagList nbtList = nbt.getTagList("fluids");

		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound fluidNBT = (NBTTagCompound) nbtList.tagAt(i);
			fluids.add(FluidStack.loadFluidStackFromNBT(fluidNBT));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

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

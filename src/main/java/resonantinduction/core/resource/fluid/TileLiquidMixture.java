package resonantinduction.core.resource.fluid;

import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.core.resource.ResourceGenerator;
import calclavia.lib.prefab.tile.TileAdvanced;

/**
 * @author Calclavia
 * 
 */
public class TileLiquidMixture extends TileAdvanced
{
	public final Set<ItemStack> items = new HashSet<ItemStack>();
	public final Set<FluidStack> fluids = new HashSet<FluidStack>();

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public boolean mix(ItemStack itemStack)
	{
		if (MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER, itemStack).length > 0)
		{
			// TODO: Maybe we need to merge the stacks?
			items.add(itemStack);
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}

		return false;
	}

	public void mix(FluidStack fluid)
	{
		if (!fluid.getFluid().isGaseous())
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
	}

	/**
	 * @return The color of the liquid based on the fluidStacks stored.
	 */
	@SideOnly(Side.CLIENT)
	public int getColor()
	{
		for (ItemStack item : items)
		{
			return ResourceGenerator.getAverageColor(item);
		}

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
		items.clear();

		NBTTagList fluidList = nbt.getTagList("Fluids");

		for (int i = 0; i < fluidList.tagCount(); ++i)
		{
			NBTTagCompound fluidNBT = (NBTTagCompound) fluidList.tagAt(i);
			fluids.add(FluidStack.loadFluidStackFromNBT(fluidNBT));
		}

		NBTTagList itemList = nbt.getTagList("Items");

		for (int i = 0; i < itemList.tagCount(); ++i)
		{
			NBTTagCompound stackTag = (NBTTagCompound) itemList.tagAt(i);
			items.add(ItemStack.loadItemStackFromNBT(stackTag));
		}
	}

	public void writeFluidToNBT(NBTTagCompound nbt)
	{
		NBTTagList fluidList = new NBTTagList();

		for (FluidStack fluid : fluids)
		{
			NBTTagCompound nbtElement = new NBTTagCompound();
			fluid.writeToNBT(nbtElement);
			fluidList.appendTag(nbtElement);
		}

		nbt.setTag("Fluids", fluidList);

		NBTTagList itemList = new NBTTagList();

		for (ItemStack itemStack : items)
		{
			NBTTagCompound var4 = new NBTTagCompound();
			itemStack.writeToNBT(var4);
			itemList.appendTag(var4);
		}

		nbt.setTag("Items", itemList);
	}
}

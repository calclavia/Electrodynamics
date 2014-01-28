package resonantinduction.archaic.process;

import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.fluid.BlockFluidMaterial;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.TileExternalInventory;

/**
 * Turns molten fuilds into ingots.
 * 
 * 1 m^3 of molten fluid = 1 block
 * Approximately 110 L of fluid = 1 ingot.
 * 
 * @author Calclavia
 * 
 */
public class TileCast extends TileExternalInventory implements IFluidHandler
{
	protected FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	private final int amountPerIngot = 110;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void onInventoryChanged()
	{
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void updateEntity()
	{
		/**
		 * Check blocks above for fluid.
		 */
		Vector3 checkPos = new Vector3(this).translate(0, 1, 0);
		int blockID = checkPos.getBlockID(worldObj);
		Block block = Block.blocksList[blockID];

		if (block instanceof BlockFluidMaterial)
		{
			// TODO: Fix overfill.
			tank.fill(((BlockFluidMaterial) block).drain(worldObj, checkPos.intX(), checkPos.intY(), checkPos.intZ(), true), true);
		}

		/**
		 * Attempt to cast the fluid
		 */
		if (tank.getFluidAmount() > amountPerIngot)
		{
			String fluidName = tank.getFluid().getFluid().getName();
			String materialName = fluidName.replace("molten", "");
			String nameCaps = materialName.substring(0, 1).toUpperCase() + materialName.substring(1);
			String ingotName = "ingot" + nameCaps;
			
			if (OreDictionary.getOres(ingotName).size() > 0)
			{
				ItemStack stack = OreDictionary.getOres(ingotName).get(0);
				incrStackSize(0, stack);
				tank.drain(amountPerIngot, true);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		tank.writeToNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tank.readFromNBT(tag);
	}

	/* IFluidHandler */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		int fill = tank.fill(resource, doFill);
		updateEntity();
		return fill;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return fluid != null && fluid.getName().contains("molten");
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { tank.getInfo() };
	}
}

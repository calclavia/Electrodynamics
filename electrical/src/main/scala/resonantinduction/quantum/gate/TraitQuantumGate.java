package resonantinduction.quantum.gate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import resonant.api.blocks.IBlockFrequency;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitQuantumGate extends TileMultipart implements IQuantumGate
{
	public PartQuantumGlyph get()
	{
		for (TMultiPart part : jPartList())
		{
			if (part instanceof PartQuantumGlyph)
			{
				return ((PartQuantumGlyph) part);
			}
		}

		return null;
	}

	@Override
	public FluidTank getQuantumTank()
	{
		return get().getQuantumTank();
	}

	@Override
	public void transport(Entity entity)
	{
		get().transport(entity);
	}

	@Override
	public int getFrequency()
	{
		int frequency = 0;

		int i = 0;

		for (TMultiPart part : jPartList())
		{
			if (part instanceof IQuantumGate)
			{
				frequency += Math.pow(PartQuantumGlyph.MAX_GLYPH, i) * ((IBlockFrequency) part).getFrequency();
				i++;
			}
		}
		if (i >= 8)
			return frequency;

		return -1;
	}

	@Override
	public void setFrequency(int frequency)
	{

	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return get().fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return get().drain(from, resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return get().drain(from, maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return get().canFill(from, fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return get().canDrain(from, fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return get().getTankInfo(from);
	}

	@Override
	public int getSizeInventory()
	{
		return get().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return get().getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return get().decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return get().getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		get().setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInvName()
	{
		return get().getInvName();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return get().isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return get().getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return get().isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return get().isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return get().getAccessibleSlotsFromSide(var1);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return get().canInsertItem(i, itemstack, j);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return get().canExtractItem(i, itemstack, j);
	}

}

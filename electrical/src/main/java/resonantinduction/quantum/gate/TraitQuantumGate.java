package resonantinduction.quantum.gate;

import icbm.api.IBlockFrequency;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mffs.api.fortron.FrequencyGrid;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import universalelectricity.api.vector.VectorWorld;
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

}

package resonantinduction.quantum.gate;

import icbm.api.IBlockFrequency;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mffs.api.fortron.FrequencyGrid;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.VectorWorld;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitQuantumGate extends TileMultipart implements IQuantumGate
{
	@Override
	public void transport(Entity entity)
	{

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

}

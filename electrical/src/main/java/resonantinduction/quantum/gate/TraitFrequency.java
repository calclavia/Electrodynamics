package resonantinduction.quantum.gate;

import icbm.api.IBlockFrequency;

import java.util.Iterator;

import net.minecraft.block.Block;
import scala.collection.Seq;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitFrequency extends TileMultipart implements IBlockFrequency
{
	@Override
	public int getFrequency()
	{
		int frequency = 0;

		int i = 0;

		for (TMultiPart part : jPartList())
		{
			if (part instanceof IBlockFrequency)
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

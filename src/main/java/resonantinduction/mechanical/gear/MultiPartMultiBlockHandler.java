package resonantinduction.mechanical.gear;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.multiblock.reference.MultiBlockHandler;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class MultiPartMultiBlockHandler extends MultiBlockHandler<PartGear>
{
	public MultiPartMultiBlockHandler(PartGear wrapper)
	{
		super(wrapper);
	}

	public PartGear getWrapperAt(Vector3 position)
	{
		TileEntity tile = position.getTileEntity(self.getWorld());

		if (tile instanceof TileMultipart)
		{
			TMultiPart part = ((TileMultipart) tile).partMap(getPlacementSide().ordinal());

			if (part instanceof PartGear)
			{
				return (PartGear) part;
			}
		}

		return null;
	}

	public ForgeDirection getPlacementSide()
	{
		return self.placementSide;
	}
}

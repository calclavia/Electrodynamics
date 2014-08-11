package resonantinduction.mechanical.gear;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.multiblock.reference.MultiBlockHandler;
import universalelectricity.core.transform.vector.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class GearMultiBlockHandler extends MultiBlockHandler<PartGear>
{
	public GearMultiBlockHandler(PartGear wrapper)
	{
		super(wrapper);
	}

	@Override
	public PartGear getWrapperAt(Vector3 position)
	{
		TileEntity tile = position.getTileEntity(this.tile.getWorld());

		if (tile instanceof TileMultipart)
		{
			TMultiPart part = ((TileMultipart) tile).partMap(getPlacementSide().ordinal());

			if (part instanceof PartGear)
			{
				if (((PartGear) part).tier == this.tile.tier)
				{
					return (PartGear) part;
				}
			}
		}

		return null;
	}

	public ForgeDirection getPlacementSide()
	{
		return tile.placementSide;
	}
}

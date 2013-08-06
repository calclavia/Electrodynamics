package resonantinduction.wire;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.compatibility.TileEntityUniversalConductor;
import universalelectricity.core.vector.Vector3;

public class TileEntityWire extends TileEntityUniversalConductor
{
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		Vector3 connectPos = new Vector3(this).modifyPositionFromSide(direction);

		if (connectPos.getTileEntity(this.worldObj) instanceof TileEntityWire && connectPos.getBlockMetadata(this.worldObj) != this.getTypeID())
		{
			return false;
		}

		return true;
	}

	@Override
	public float getResistance()
	{
		return getMaterial().resistance;
	}

	@Override
	public float getCurrentCapacity()
	{
		return getMaterial().maxAmps;
	}

	public EnumWire getMaterial()
	{
		return EnumWire.values()[this.getTypeID()];
	}

	public int getTypeID()
	{
		return this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
	}
}

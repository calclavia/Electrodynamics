package resonantinduction.wire;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.BlockFurnace;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import universalelectricity.compatibility.TileEntityUniversalConductor;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

public class TileEntityWire extends TileEntityUniversalConductor
{
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
		{
			return false;
		}

		Vector3 connectPos = new Vector3(this).modifyPositionFromSide(direction);

		if (connectPos.getTileEntity(this.worldObj) instanceof TileEntityWire && connectPos.getBlockMetadata(this.worldObj) != this.getTypeID())
		{
			return false;
		}

		return true;
	}

	@Override
	public void refresh()
	{
		if (!this.worldObj.isRemote)
		{
			this.adjacentConnections = null;

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if (this.canConnect(side.getOpposite()))
				{
					TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this), side);

					if (tileEntity != null)
					{
						if (tileEntity.getClass() == this.getClass() && tileEntity instanceof INetworkProvider)
						{
							this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
						}
					}
				}
			}

			this.getNetwork().refresh();
		}
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

	public EnumWireMaterial getMaterial()
	{
		return EnumWireMaterial.values()[this.getTypeID()];
	}

	public int getTypeID()
	{
		return this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
	}
}

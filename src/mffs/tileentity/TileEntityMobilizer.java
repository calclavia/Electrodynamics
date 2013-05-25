package mffs.tileentity;

import java.util.Iterator;
import java.util.List;

import mffs.base.TileEntityModuleAcceptor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

public class TileEntityMobilizer extends TileEntityModuleAcceptor
{
	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.isActive())
		{
			this.updatePushedObjects(1, 0.25f);

			if (!this.worldObj.isRemote)
			{
				ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
				this.moveBlock(new Vector3(this).modifyPositionFromSide(dir), dir);
			}
		}
	}

	protected void moveBlock(Vector3 position, ForgeDirection direction)
	{
		Vector3 newPosition = position.clone().modifyPositionFromSide(direction);

		TileEntity tileEntity = position.getTileEntity(this.worldObj);
		int blockID = position.getBlockID(this.worldObj);
		int blockMetadata = position.getBlockMetadata(this.worldObj);

		if (blockID > 0 && newPosition.getBlockID(this.worldObj) == 0)
		{
			if (tileEntity != null)
			{
				NBTTagCompound tileData = new NBTTagCompound();
				tileEntity.writeToNBT(tileData);
				this.worldObj.removeBlockTileEntity(position.intX(), position.intY(), position.intZ());
				position.setBlock(this.worldObj, 0);
				newPosition.setBlock(this.worldObj, blockID);
				this.worldObj.setBlockMetadataWithNotify(newPosition.intX(), newPosition.intY(), newPosition.intZ(), blockMetadata, 2);
				TileEntity newTile = newPosition.getTileEntity(this.worldObj);
				newTile.readFromNBT(tileData);
				newTile.xCoord = position.intX();
				newTile.yCoord = position.intY();
				newTile.zCoord = position.intZ();
				this.worldObj.setBlockTileEntity(position.intX(), position.intY(), position.intZ(), newTile);
			}
			else
			{
				position.setBlock(this.worldObj, 0);
				newPosition.setBlock(this.worldObj, blockID, blockMetadata);
			}
		}
	}

	private void updatePushedObjects(float distance, float amount)
	{
		ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		AxisAlignedBB axisalignedbb = this.getSearchAxisAlignedBB(distance, dir.ordinal());

		if (axisalignedbb != null)
		{
			List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

			Iterator<Entity> iterator = list.iterator();

			while (iterator.hasNext())
			{
				Entity entity = iterator.next();
				entity.moveEntity((double) (amount * dir.offsetX), (double) (amount * dir.offsetY), (double) (amount * dir.offsetZ));
			}
		}
	}

	public AxisAlignedBB getSearchAxisAlignedBB(float distance, int direction)
	{
		AxisAlignedBB axisalignedbb = this.getBlockType().getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord);

		if (axisalignedbb == null)
		{
			return null;
		}
		else
		{
			axisalignedbb.maxY += distance;
			return axisalignedbb;
		}

	}

	@Override
	public int getSizeInventory()
	{
		return 0;
	}

}

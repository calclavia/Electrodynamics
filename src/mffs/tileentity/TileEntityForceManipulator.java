package mffs.tileentity;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

public class TileEntityForceManipulator extends TileEntityFieldInteraction
{
	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.isActive())
			{
				this.updatePushedObjects(1, 0.25f);
				ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
				this.moveBlock(new Vector3(this).modifyPositionFromSide(dir), dir);
			}
		}
	}

	protected void moveBlock(Vector3 position, ForgeDirection direction)
	{
		if (!this.worldObj.isRemote)
		{
			Vector3 newPosition = position.clone().modifyPositionFromSide(direction);

			TileEntity tileEntity = position.getTileEntity(this.worldObj);
			int blockID = position.getBlockID(this.worldObj);

			if (blockID > 0 && newPosition.getBlockID(this.worldObj) == 0)
			{
				int blockMetadata = position.getBlockMetadata(this.worldObj);

				if (tileEntity != null)
				{
					this.worldObj.removeBlockTileEntity(position.intX(), position.intY(), position.intZ());
					position.setBlock(this.worldObj, 0);
					newPosition.setBlock(this.worldObj, blockID, blockMetadata);
					NBTTagCompound tileData = new NBTTagCompound();
					tileEntity.writeToNBT(tileData);
					TileEntity newTile = newPosition.getTileEntity(this.worldObj);
					newTile.readFromNBT(tileData);
					newTile.worldObj = this.worldObj;
					newTile.xCoord = newPosition.intX();
					newTile.yCoord = newPosition.intY();
					newTile.zCoord = newPosition.intZ();
					tileEntity.validate();
					/*
					 * tileEntity.worldObj = this.worldObj; tileEntity.xCoord = newPosition.intX();
					 * tileEntity.yCoord = newPosition.intY(); tileEntity.zCoord =
					 * newPosition.intZ(); tileEntity.validate();
					 * this.worldObj.setBlockTileEntity(newPosition.intX(), newPosition.intY(),
					 * newPosition.intZ(), tileEntity);
					 */
				}
				else
				{
					position.setBlock(this.worldObj, 0);
					newPosition.setBlock(this.worldObj, blockID, blockMetadata);
				}
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
				entity.moveEntity(amount * dir.offsetX, amount * dir.offsetY, amount * dir.offsetZ);
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
		return 3 + 18;
	}

}

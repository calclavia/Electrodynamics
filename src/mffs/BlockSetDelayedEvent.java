package mffs;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public class BlockSetDelayedEvent extends DelayedEvent
{
	private World world;
	private Vector3 position;
	private Vector3 newPosition;

	public BlockSetDelayedEvent(int ticks, World world, Vector3 position, Vector3 newPosition)
	{
		super(ticks);
		this.world = world;
		this.position = position;
		this.newPosition = newPosition;
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			TileEntity tileEntity = position.getTileEntity(this.world);
			int blockID = position.getBlockID(this.world);

			if (blockID > 0 && newPosition.getBlockID(this.world) == 0)
			{
				if (Block.blocksList[blockID].getBlockHardness(this.world, position.intX(), position.intY(), position.intZ()) != -1)
				{
					int blockMetadata = position.getBlockMetadata(this.world);

					if (tileEntity != null)
					{
						this.world.removeBlockTileEntity(position.intX(), position.intY(), position.intZ());
						position.setBlock(this.world, 0);
						newPosition.setBlock(this.world, blockID);
						this.world.setBlockMetadataWithNotify(newPosition.intX(), newPosition.intY(), newPosition.intZ(), blockMetadata, 3);
						NBTTagCompound tileData = new NBTTagCompound();
						tileEntity.writeToNBT(tileData);
						TileEntity newTile = newPosition.getTileEntity(this.world);
						newTile.readFromNBT(tileData);
						newTile.worldObj = this.world;
						newTile.xCoord = newPosition.intX();
						newTile.yCoord = newPosition.intY();
						newTile.zCoord = newPosition.intZ();
						tileEntity.validate();
						/*
						 * tileEntity.worldObj = this.worldObj; tileEntity.xCoord =
						 * newPosition.intX(); tileEntity.yCoord = newPosition.intY();
						 * tileEntity.zCoord = newPosition.intZ(); tileEntity.validate();
						 * this.worldObj.setBlockTileEntity(newPosition.intX(), newPosition.intY(),
						 * newPosition.intZ(), tileEntity);
						 */
					}
					else
					{
						position.setBlock(this.world, 0);
						newPosition.setBlock(this.world, blockID, blockMetadata);
					}
				}
			}
		}
	}
}

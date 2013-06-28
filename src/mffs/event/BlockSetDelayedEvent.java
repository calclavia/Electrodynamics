package mffs.event;

import mffs.DelayedEvent;
import mffs.api.ForceManipulator.ISpecialForceManipulation;
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

	private NBTTagCompound tileData = null;
	private int blockID = 0;
	private int blockMetadata = 0;

	public BlockSetDelayedEvent(int ticks, World world, Vector3 position, Vector3 newPosition)
	{
		super(ticks);
		this.world = world;
		this.position = position;
		this.newPosition = newPosition;

		this.blockID = position.getBlockID(this.world);
		this.blockMetadata = position.getBlockMetadata(this.world);

		TileEntity tileEntity = position.getTileEntity(this.world);

		if (tileEntity != null)
		{
			this.tileData = new NBTTagCompound();
			tileEntity.writeToNBT(this.tileData);
		}
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			// TileEntity tileEntity = position.getTileEntity(this.world);
			// int blockID = position.getBlockID(this.world);

			if (blockID > 0)
			{
				if (Block.blocksList[blockID].getBlockHardness(this.world, position.intX(), position.intY(), position.intZ()) != -1)
				{
					// int blockMetadata = position.getBlockMetadata(this.world);
					try
					{
						if (this.tileData != null)
						{
							// this.world.removeBlockTileEntity(position.intX(), position.intY(),
							// position.intZ());
							// position.setBlock(this.world, 0, 0, 3);
							this.newPosition.setBlock(this.world, this.blockID, this.blockMetadata, 2);
							this.world.setBlockMetadataWithNotify(this.newPosition.intX(), newPosition.intY(), newPosition.intZ(), this.blockMetadata, 2);
							// NBTTagCompound tileData = new NBTTagCompound();
							// tileEntity.writeToNBT(tileData);
							TileEntity newTile = this.newPosition.getTileEntity(this.world);
							newTile.readFromNBT(tileData);
							newTile.worldObj = this.world;
							newTile.xCoord = this.newPosition.intX();
							newTile.yCoord = this.newPosition.intY();
							newTile.zCoord = this.newPosition.intZ();
							newTile.validate();

							if (newTile instanceof ISpecialForceManipulation)
							{
								((ISpecialForceManipulation) newTile).postMove();
							}
						}
						else
						{
							// position.setBlock(this.world, 0);
							newPosition.setBlock(this.world, blockID, blockMetadata);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}

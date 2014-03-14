package mffs.block;

import mffs.base.BlockMFFS;
import mffs.tile.TileForceManipulator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockForceManipulator extends BlockMFFS
{
	public BlockForceManipulator(int i)
	{
		super(i, "manipulator");
		this.rotationMask = 63;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileForceManipulator();
	}
}
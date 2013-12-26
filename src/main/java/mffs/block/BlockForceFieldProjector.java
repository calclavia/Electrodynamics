package mffs.block;

import mffs.base.BlockMFFS;
import mffs.tileentity.TileForceFieldProjector;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockForceFieldProjector extends BlockMFFS
{
	public BlockForceFieldProjector(int id)
	{
		super(id, "projector");
		this.setBlockBounds(0, 0, 0, 1, 0.8f, 1);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileForceFieldProjector();
	}

	@Override
	public int getLightValue(IBlockAccess iBlockAccess, int x, int y, int z)
	{
		TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileForceFieldProjector)
		{
			if (((TileForceFieldProjector) tileEntity).getMode() != null)
			{
				return 10;
			}
		}

		return super.getLightValue(iBlockAccess, x, y, z);
	}
}
package mffs.block;

import mffs.base.BlockMachine;
import mffs.tileentity.TileEntityForceFieldProjector;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockForceFieldProjector extends BlockMachine
{
	public BlockForceFieldProjector(int id)
	{
		super(id, "projector");
		this.setBlockBounds(0, 0, 0, 1, 0.8f, 1);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityForceFieldProjector();
	}

	@Override
	public int getLightValue(IBlockAccess iBlockAccess, int x, int y, int z)
	{
		TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityForceFieldProjector)
		{
			if (((TileEntityForceFieldProjector) tileEntity).getMode() != null)
			{
				return 10;
			}
		}

		return super.getLightValue(iBlockAccess, x, y, z);
	}
}
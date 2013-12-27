package mffs.block;

import mffs.base.BlockMFFS;
import mffs.tile.TileCoercionDeriver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCoercionDeriver extends BlockMFFS
{
	public BlockCoercionDeriver(int i)
	{
		super(i, "coercionDeriver");
		this.setBlockBounds(0, 0, 0, 1, 0.8f, 1);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileCoercionDeriver();
	}
}
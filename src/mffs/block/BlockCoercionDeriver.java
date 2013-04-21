package mffs.block;

import mffs.base.BlockMachine;
import mffs.tileentity.TileEntityCoercionDeriver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCoercionDeriver extends BlockMachine
{
	public BlockCoercionDeriver(int i)
	{
		super(i, "coercionDeriver");
		this.setBlockBounds(0, 0, 0, 1, 0.8f, 1);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityCoercionDeriver();
	}
}
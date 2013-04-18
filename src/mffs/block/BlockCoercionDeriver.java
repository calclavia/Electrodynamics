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
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityCoercionDeriver();
	}
}
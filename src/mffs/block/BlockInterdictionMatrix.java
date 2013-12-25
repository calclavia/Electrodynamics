package mffs.block;

import mffs.tileentity.TileInterdictionMatrix;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInterdictionMatrix extends BlockMachineBlock
{
	public BlockInterdictionMatrix(int i)
	{
		super(i, "interdictionMatrix");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileInterdictionMatrix();
	}
}
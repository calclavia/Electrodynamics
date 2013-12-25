package mffs.block;

import mffs.base.BlockMFFS;
import mffs.tileentity.TileFortronCapacitor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFortronCapacitor extends BlockMFFS
{
	public BlockFortronCapacitor(int i)
	{
		super(i, "fortronCapacitor");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileFortronCapacitor();
	}
}
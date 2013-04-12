package mffs.block;

import mffs.base.BlockMachine;
import mffs.tileentity.TileEntityFortronCapacitor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFortronCapacitor extends BlockMachine
{
	public BlockFortronCapacitor(int i)
	{
		super(i, "fortronCapacitor");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityFortronCapacitor();
	}
}
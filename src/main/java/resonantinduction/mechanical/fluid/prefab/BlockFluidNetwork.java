package resonantinduction.mechanical.fluid.prefab;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockRI;

/**
 * @author Calclavia
 * 
 */
public class BlockFluidNetwork extends BlockRI
{
	public BlockFluidNetwork(String name)
	{
		super(name);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileFluidNetwork)
		{
			((TileFluidNetwork) tile).refresh();
			((TileFluidNetwork) tile).getNetwork().reconstruct();
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int par5)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileFluidNetwork)
		{
			((TileFluidNetwork) tile).refresh();
		}
	}

}

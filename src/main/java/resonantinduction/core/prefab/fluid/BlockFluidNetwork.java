package resonantinduction.core.prefab.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import calclavia.lib.prefab.block.BlockTile;

/** @author Calclavia */
public abstract class BlockFluidNetwork extends BlockTile
{
	public BlockFluidNetwork(int id, Material material)
	{
		super(id, material);
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

package resonantinduction.mechanical.process;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import calclavia.lib.prefab.block.BlockTile;

/**
 * Used for filtering liquid mixtures
 * 
 * @author Calclavia
 * 
 */
public class BlockFilter extends BlockTile
{
	public BlockFilter(int id)
	{
		super(id, Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileFilter();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

}

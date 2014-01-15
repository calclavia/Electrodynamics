package resonantinduction.electrical.purifier;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.block.BlockRIRotatable;
import resonantinduction.mechanical.grinder.TilePurifier;
import universalelectricity.api.vector.VectorWorld;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockPurifier extends BlockRIRotatable implements ITileEntityProvider
{
	public BlockPurifier()
	{
		super("purifier", Settings.getNextBlockID());
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		this.checkConflicts(world, x, y, z);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int par5)
	{
		this.checkConflicts(world, x, y, z);
	}

	/**
	 * Checks for any conflicting directions with other grinders.
	 */
	private void checkConflicts(World world, int x, int y, int z)
	{
		ForgeDirection facing = this.getDirection(world, x, y, z);

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			if (dir != facing && dir != facing.getOpposite())
			{
				VectorWorld checkPos = (VectorWorld) new VectorWorld(world, x, y, z).modifyPositionFromSide(dir);
				TileEntity tileEntity = checkPos.getTileEntity();

				if (tileEntity instanceof TilePurifier)
				{
					if (this.getDirection(world, checkPos.intX(), checkPos.intY(), checkPos.intZ()) == facing)
					{
						this.dropBlockAsItem(world, x, y, z, 0, 0);
						world.setBlockToAir(x, y, z);
					}
				}
			}
		}
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TilePurifier();
	}
}

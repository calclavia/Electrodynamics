package resonantinduction.mechanics.purifier;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.base.BlockRotatableBase;
import resonantinduction.mechanics.machine.grinder.TilePurifier;
import universalelectricity.api.vector.VectorWorld;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockPurifier extends BlockRotatableBase implements ITileEntityProvider
{
	public BlockPurifier(int id)
	{
		super("purifier", id);
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

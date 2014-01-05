package resonantinduction.machine.grinder;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.base.BlockBase;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockGrinderWheel extends BlockBase implements ITileEntityProvider
{
	public BlockGrinderWheel(int id)
	{
		super("grindingWheel", id);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileGrinderWheel();
	}
}

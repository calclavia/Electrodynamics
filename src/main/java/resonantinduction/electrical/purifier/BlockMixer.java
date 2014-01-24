package resonantinduction.electrical.purifier;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.block.BlockRIRotatable;
import resonantinduction.mechanical.process.TileMixer;
import universalelectricity.api.vector.VectorWorld;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockMixer extends BlockRIRotatable implements ITileEntityProvider
{
	public BlockMixer()
	{
		super("mixer", Settings.getNextBlockID());
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
		return new TileMixer();
	}
}

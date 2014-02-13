package resonantinduction.mechanical.purifier;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.mechanical.process.TileMixer;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockRotatable;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockMixer extends BlockRotatable implements ITileEntityProvider
{
	public BlockMixer(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_metal_top");
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

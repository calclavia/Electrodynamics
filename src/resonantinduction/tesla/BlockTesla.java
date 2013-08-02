/**
 * 
 */
package resonantinduction.tesla;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.base.BlockBase;

/**
 * @author Calclavia
 * 
 */
public class BlockTesla extends BlockBase implements ITileEntityProvider
{
	public BlockTesla(int id)
	{
		super("tesla", id, Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityTesla();
	}
}

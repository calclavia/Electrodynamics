package resonantinduction;

import net.minecraft.block.BlockFurnace;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author Calclavia
 * 
 */
public class BlockAdvancedFurnace extends BlockFurnace
{
	protected BlockAdvancedFurnace(int par1, boolean par2)
	{
		super(par1, par2);
	}

	@Override
	public TileEntity createNewTileEntity(World par1World)
	{
		return new TileEntityAdvancedFurnace();
	}
}

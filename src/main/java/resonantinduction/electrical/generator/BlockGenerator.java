package resonantinduction.electrical.generator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockRIRotatable;
import resonantinduction.core.render.RIBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGenerator extends BlockRIRotatable
{
	public BlockGenerator()
	{
		super("generator");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileGenerator();
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

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RIBlockRenderingHandler.ID;
	}
}

package resonantinduction.electrical.generator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockRI;
import resonantinduction.electrical.render.ElectricalBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGenerator extends BlockRI
{
	public BlockGenerator()
	{
		super("generator");
		this.setBlockBounds(0, 0, 0, 1, 0.5f, 1);
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
		return ElectricalBlockRenderingHandler.ID;
	}
}

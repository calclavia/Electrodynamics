package resonantinduction.electrical.generator.solar;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockMachine;
import resonantinduction.electrical.render.ElectricalBlockRenderingHandler;
import resonantinduction.old.client.render.BlockRenderingHandler;

public class BlockSolarPanel extends BlockMachine
{
	public BlockSolarPanel()
	{
		super("solarPanel");
		this.setBlockBounds(0, 0, 0, 1, 0.5f, 1);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileSolarPanel();
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

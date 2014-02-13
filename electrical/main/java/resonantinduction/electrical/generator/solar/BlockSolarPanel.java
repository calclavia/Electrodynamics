package resonantinduction.electrical.generator.solar;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSolarPanel extends BlockTile
{
	public BlockSolarPanel(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_metal_side");
		setBlockBounds(0, 0, 0, 1, 0.5f, 1);
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
		return RIBlockRenderingHandler.ID;
	}
}

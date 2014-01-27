package resonantinduction.electrical.generator.thermopile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRI;
import resonantinduction.core.render.RIBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockThermopile extends BlockRI
{
	public BlockThermopile()
	{
		super("thermopile");
		setTextureName(Reference.PREFIX + "material_metal_top");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileThermopile();
	}
}

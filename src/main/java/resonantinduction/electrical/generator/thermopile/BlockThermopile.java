package resonantinduction.electrical.generator.thermopile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockTile;

public class BlockThermopile extends BlockTile
{
	public BlockThermopile(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_metal_top");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileThermopile();
	}
}

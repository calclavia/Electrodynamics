package resonantinduction.electrical.generator.thermopile;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonant.lib.prefab.block.BlockTile;
import resonantinduction.core.Reference;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockThermopile extends BlockTile
{
	public Icon topIcon;

	public BlockThermopile(int id)
	{
		super(id, UniversalElectricity.machine);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		topIcon = iconReg.registerIcon(Reference.PREFIX + "thermopile_top");
		super.registerIcons(iconReg);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 1)
		{
			return topIcon;
		}

		return blockIcon;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileThermopile();
	}
}

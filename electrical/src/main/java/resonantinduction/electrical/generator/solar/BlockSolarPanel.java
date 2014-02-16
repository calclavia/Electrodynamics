package resonantinduction.electrical.generator.solar;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RIBlockRenderingHandler;
import resonantinduction.electrical.battery.TileEnergyDistribution;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSolarPanel extends BlockTile
{
	public Icon sideIcon;
	public Icon bottomIcon;

	public BlockSolarPanel(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "solarPanel_top");
		setBlockBounds(0, 0, 0, 1, 0.3f, 1);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			TileEnergyDistribution distribution = (TileEnergyDistribution) world.getBlockTileEntity(x, y, z);
			distribution.updateStructure();
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		if (!world.isRemote)
		{
			if (id == blockID)
			{
				TileEnergyDistribution distribution = (TileEnergyDistribution) world.getBlockTileEntity(x, y, z);
				distribution.updateStructure();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		sideIcon = iconReg.registerIcon(Reference.PREFIX + "solarPanel_side");
		bottomIcon = iconReg.registerIcon(Reference.PREFIX + "solarPanel_bottom");
		super.registerIcons(iconReg);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 0)
		{
			return bottomIcon;
		}
		else if (side == 1)
		{
			return blockIcon;
		}

		return sideIcon;
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

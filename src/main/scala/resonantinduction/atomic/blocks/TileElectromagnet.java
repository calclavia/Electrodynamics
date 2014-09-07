package resonantinduction.atomic.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import resonant.api.IElectromagnet;
import resonant.content.prefab.itemblock.ItemBlockMetadata;
import resonant.content.spatial.block.SpatialBlock;

import java.util.List;

/**
 * Electromagnet block
 */
public class TileElectromagnet extends SpatialBlock implements IElectromagnet
{
	private static IIcon iconTop, iconGlass;

	public TileElectromagnet()
	{
		super(Material.iron);
		blockResistance(20);
		forceStandardRender(true);
		normalRender(false);
		isOpaqueCube(false);
		this.itemBlock(ItemBlockMetadata.class);
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		if (metadata == 1)
		{
			return iconGlass;
		}

		if (side == 0 || side == 1)
		{
			return iconTop;
		}

		return super.getIcon(side, metadata);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		iconTop = iconRegister.registerIcon(domain() + textureName() + "_top");
		iconGlass = iconRegister.registerIcon(domain() + "electromagnetGlass");
	}

	@Override
	public int metadataDropped(int meta, int fortune)
	{
		return meta;
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side)
	{
		return true; // access.getBlockId(x, y, z) == blockID() && access.getBlockMetadata(x, y, z) == 1 ? false : super.shouldSideBeRendered(access, x, y, z, side);
	}

	@Override
	public int getRenderBlockPass()
	{
		return 0;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List par3List)
	{
		super.getSubBlocks(item, par2CreativeTabs, par3List);
		par3List.add(new ItemStack(item, 1, 1));
	}

	@Override
	public boolean isRunning()
	{
		return true;
	}
}

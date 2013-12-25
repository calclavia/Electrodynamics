package mffs.block;

import mffs.base.BlockMFFS;
import mffs.base.TileMFFS;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;

public class BlockMachineBlock extends BlockMFFS
{
	protected Icon blockIconTop, blockIconOn, blockIconTopOn;

	public BlockMachineBlock(int id, String name)
	{
		super(id, name);
	}

	@Override
	public Icon getBlockTexture(IBlockAccess par1IBlockAccess, int x, int y, int z, int side)
	{
		TileEntity tileEntity = par1IBlockAccess.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMFFS)
		{
			if (((TileMFFS) tileEntity).isActive())
			{
				if (side == 0 || side == 1)
				{
					return this.blockIconTopOn;
				}

				return this.blockIconOn;
			}
		}

		if (side == 0 || side == 1)
		{
			return this.blockIconTop;
		}

		return this.blockIcon;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(this.getTextureName());
		this.blockIconTop = par1IconRegister.registerIcon(this.getTextureName() + "_top");
		this.blockIconOn = par1IconRegister.registerIcon(this.getTextureName() + "_on");
		this.blockIconTopOn = par1IconRegister.registerIcon(this.getTextureName() + "_top_on");
	}

	@Override
	public boolean isOpaqueCube()
	{
		return true;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return true;
	}

	@Override
	public int getRenderType()
	{
		return 0;
	}
}

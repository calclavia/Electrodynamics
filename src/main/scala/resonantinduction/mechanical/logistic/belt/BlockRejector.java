package resonantinduction.mechanical.logistic.belt;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import resonant.lib.render.block.BlockRenderingHandler;
import resonantinduction.core.Reference;
import resonantinduction.archaic.filter.imprint.BlockImprintable;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRejector extends BlockImprintable
{
	@SideOnly(Side.CLIENT)
	protected IIcon front;

	public BlockRejector(int id)
	{
		super(id, UniversalElectricity.machine);
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileRejector();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.ID;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int damageDropped(int par1)
	{
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister par1IIconRegister)
	{
		this.blockIcon = par1IIconRegister.registerIcon(Reference.PREFIX + "imprinter_bottom");
		this.front = par1IIconRegister.registerIcon(Reference.PREFIX + "disk_tray");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata)
	{
		if (side == metadata)
		{
			return this.front;
		}
		return this.blockIcon;
	}

}

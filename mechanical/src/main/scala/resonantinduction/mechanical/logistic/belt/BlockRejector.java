package resonantinduction.mechanical.logistic.belt;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonant.lib.render.block.BlockRenderingHandler;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.imprint.BlockImprintable;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRejector extends BlockImprintable
{
	@SideOnly(Side.CLIENT)
	protected Icon front;

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
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(Reference.PREFIX + "imprinter_bottom");
		this.front = par1IconRegister.registerIcon(Reference.PREFIX + "disk_tray");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		if (side == metadata)
		{
			return this.front;
		}
		return this.blockIcon;
	}

}

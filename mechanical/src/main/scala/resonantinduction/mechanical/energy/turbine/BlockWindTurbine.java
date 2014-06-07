package resonantinduction.mechanical.energy.turbine;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonant.lib.render.block.BlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWindTurbine extends BlockTurbine
{
	public BlockWindTurbine(int id)
	{
		super(id);
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int i = 0; i < 3; i++)
			par3List.add(new ItemStack(par1, 1, i));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.ID;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileWindTurbine();
	}
}

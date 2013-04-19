package mffs.block;

import mffs.base.BlockMachine;
import mffs.tileentity.TileEntityCoercionDeriver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCoercionDeriver extends BlockMachine
{
	public BlockCoercionDeriver(int i)
	{
		super(i, "coercionDeriver");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityCoercionDeriver();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return 0;
	}
}
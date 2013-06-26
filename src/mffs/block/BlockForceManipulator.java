package mffs.block;

import mffs.base.BlockMachine;
import mffs.tileentity.TileEntityForceManipulator;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class BlockForceManipulator extends BlockMachine
{
	public BlockForceManipulator(int i)
	{
		super(i, "manipulator");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityForceManipulator();
	}
}
package mffs.block;

import mffs.base.BlockMachine;
import mffs.tileentity.TileEntityForceManipulator;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockForceManipulator extends BlockMachine
{
	public BlockForceManipulator(int i)
	{
		super(i, "manipulator");
	}

	public static int determineOrientation(World world, int x, int y, int z, EntityPlayer entityPlayer)
	{
		if (MathHelper.abs((float) entityPlayer.posX - x) < 2.0F && MathHelper.abs((float) entityPlayer.posZ - z) < 2.0F)
		{
			double var5 = entityPlayer.posY + 1.82D - entityPlayer.yOffset;

			if (var5 - y > 2.0D)
			{
				return 1;
			}

			if (y - var5 > 0.0D)
			{
				return 0;
			}
		}

		int var7 = MathHelper.floor_double(entityPlayer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		return var7 == 0 ? 2 : (var7 == 1 ? 5 : (var7 == 2 ? 3 : (var7 == 3 ? 4 : 0)));
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving par5EntityLiving, ItemStack stack)
	{
		int metadata = determineOrientation(world, x, y, z, (EntityPlayer) par5EntityLiving);
		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
	}
	

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		/**
		 * NOTE! This will rotate the block only in all 4 horizontal directions. If your block
		 * rotates up or down, you should override this.
		 */
		this.setDirection(world, x, y, z, ForgeDirection.getOrientation(ForgeDirection.ROTATION_MATRIX[0][this.getDirection(world, x, y, z).ordinal()]));
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityForceManipulator();
	}
}
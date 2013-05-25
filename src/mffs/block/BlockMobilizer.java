package mffs.block;

import mffs.tileentity.TileEntityMobilizer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class BlockMobilizer extends BlockMachineBlock
{
	public BlockMobilizer(int i)
	{
		super(i, "mobilizer");
	}

	public static int determineOrientation(World world, int x, int y, int z, EntityPlayer entityPlayer)
	{
		if (MathHelper.abs((float) entityPlayer.posX - (float) x) < 2.0F && MathHelper.abs((float) entityPlayer.posZ - (float) z) < 2.0F)
		{
			double var5 = entityPlayer.posY + 1.82D - (double) entityPlayer.yOffset;

			if (var5 - (double) y > 2.0D)
			{
				return 1;
			}

			if ((double) y - var5 > 0.0D)
			{
				return 0;
			}
		}

		int var7 = MathHelper.floor_double((double) (entityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		return var7 == 0 ? 2 : (var7 == 1 ? 5 : (var7 == 2 ? 3 : (var7 == 3 ? 4 : 0)));
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving par5EntityLiving, ItemStack stack)
	{
		int metadata = determineOrientation(world, x, y, z, (EntityPlayer) par5EntityLiving);
		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityMobilizer();
	}
}
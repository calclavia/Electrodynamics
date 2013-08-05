/**
 * 
 */
package resonantinduction.multimeter;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.base.BlockBase;

/**
 * A block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class BlockMultimeter extends BlockBase implements ITileEntityProvider
{
	private Icon machineIcon;

	public BlockMultimeter(int id)
	{
		super("multimeter", id);
	}

	public static int determineOrientation(World par0World, int par1, int par2, int par3, EntityLivingBase par4EntityLivingBase)
	{
		if (MathHelper.abs((float) par4EntityLivingBase.posX - par1) < 2.0F && MathHelper.abs((float) par4EntityLivingBase.posZ - par3) < 2.0F)
		{
			double d0 = par4EntityLivingBase.posY + 1.82D - par4EntityLivingBase.yOffset;

			if (d0 - par2 > 2.0D)
			{
				return 1;
			}

			if (par2 - d0 > 0.0D)
			{
				return 0;
			}
		}

		int l = MathHelper.floor_double(par4EntityLivingBase.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		return l == 0 ? 2 : (l == 1 ? 5 : (l == 2 ? 3 : (l == 3 ? 4 : 0)));
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack)
	{
		int l = determineOrientation(world, x, y, z, par5EntityLivingBase);
		world.setBlockMetadataWithNotify(x, y, z, l, 2);
	}

	@Override
	public Icon getIcon(int side, int metadata)
	{
		if (side == metadata)
		{
			return this.blockIcon;
		}

		return this.machineIcon;
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		this.machineIcon = iconRegister.registerIcon(ResonantInduction.PREFIX + "machine");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float par7, float par8, float par9)
	{
		if (entityPlayer.isSneaking())
		{
			world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.ROTATION_MATRIX[world.getBlockMetadata(x, y, z)][side], 3);
		}
		else
		{
			entityPlayer.openGui(ResonantInduction.INSTNACE, 0, world, x, y, z);
		}

		return true;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int par5)
	{
		return this.isProvidingWeakPower(blockAccess, x, y, z, par5);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int par5)
	{
		TileEntity tile = blockAccess.getBlockTileEntity(x, y, z);

		if (tile instanceof TileEntityMultimeter)
		{
			return ((TileEntityMultimeter) tile).redstoneOn ? 14 : 0;
		}
		return 0;
	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityMultimeter();
	}
}

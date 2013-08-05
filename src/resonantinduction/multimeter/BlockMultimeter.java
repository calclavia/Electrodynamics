/**
 * 
 */
package resonantinduction.multimeter;

import static net.minecraftforge.common.ForgeDirection.EAST;
import static net.minecraftforge.common.ForgeDirection.NORTH;
import static net.minecraftforge.common.ForgeDirection.SOUTH;
import static net.minecraftforge.common.ForgeDirection.WEST;
import net.minecraft.block.Block;
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

	/**
	 * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY,
	 * hitZ, block metadata
	 */
	public int onBlockPlaced(World par1World, int par2, int par3, int par4, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		return ForgeDirection.getOrientation(side).ordinal();
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

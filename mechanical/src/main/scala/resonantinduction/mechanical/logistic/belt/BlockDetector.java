package resonantinduction.mechanical.logistic.belt;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.imprint.BlockImprintable;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** @author Briman0094 */
public class BlockDetector extends BlockImprintable
{
	Icon front_red, front_green, side_green, side_red;

	public BlockDetector(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_metal_side");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		front_green = iconReg.registerIcon(Reference.PREFIX + "detector_front_green");
		front_red = iconReg.registerIcon(Reference.PREFIX + "detector_front_red");
		side_green = iconReg.registerIcon(Reference.PREFIX + "detector_side_green");
		side_red = iconReg.registerIcon(Reference.PREFIX + "detector_side_red");
		super.registerIcons(iconReg);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess iBlockAccess, int x, int y, int z, int side)
	{
		boolean isInverted = false;
		boolean isFront = false;
		TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileDetector)
		{
			isFront = side == ((TileDetector) tileEntity).getDirection().ordinal();
			isInverted = ((TileDetector) tileEntity).isInverted();
		}

		return isInverted ? (isFront ? front_red : side_red) : (isFront ? front_green : side_green);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		if (side == ForgeDirection.SOUTH.ordinal())
		{
			return front_green;
		}

		return side_green;
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileDetector)
		{
			((TileDetector) tileEntity).toggleInversion();
			return true;
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		if (!canBlockStay(world, x, y, z))
		{
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, 0, 0, 3);
		}
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean isBlockNormalCube(World world, int x, int y, int z)
	{
		return false;
	}

	@Override
	public boolean isBlockSolid(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5)
	{
		return false;
	}

	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
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
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int direction)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileDetector)
		{
			return ((TileDetector) tileEntity).isPoweringTo(ForgeDirection.getOrientation(direction));
		}
		return 0;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int direction)
	{
		return isProvidingStrongPower(world, x, y, z, direction);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileDetector();
	}

}

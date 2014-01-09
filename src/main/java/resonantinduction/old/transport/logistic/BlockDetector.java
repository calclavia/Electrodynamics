package resonantinduction.old.transport.logistic;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockImprintable;
import resonantinduction.old.client.render.RenderDetector;
import universalelectricity.api.UniversalElectricity;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** @author Briman0094 */
public class BlockDetector extends BlockImprintable
{
	Icon eye_red;
	Icon eye_green;

	public BlockDetector()
	{
		super("detector", UniversalElectricity.machine);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
	{
		int angle = MathHelper.floor_double((entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int change = 2;

		switch (angle)
		{
			case 0:
				change = ForgeDirection.NORTH.ordinal();
				break;
			case 1:
				change = ForgeDirection.EAST.ordinal();
				break;
			case 2:
				change = ForgeDirection.SOUTH.ordinal();
				break;
			case 3:
				change = ForgeDirection.WEST.ordinal();
				break;
		}

		if (entity.rotationPitch < -70f) // up
		{
			change = ForgeDirection.DOWN.ordinal();
		}
		if (entity.rotationPitch > 70f) // down
		{
			change = ForgeDirection.UP.ordinal();
		}

		world.setBlockMetadataWithNotify(x, y, z, change, 3);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		super.registerIcons(iconReg);
		this.eye_green = iconReg.registerIcon(Reference.PREFIX + "detector_green");
		this.eye_red = iconReg.registerIcon(Reference.PREFIX + "detector_red");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess iBlockAccess, int x, int y, int z, int side)
	{
		TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityDetector)
		{
			if (side == ForgeDirection.getOrientation(iBlockAccess.getBlockMetadata(x, y, z)).ordinal())
			{
				if (((TileEntityDetector) tileEntity).isInverted())
				{
					return this.eye_red;

				}
				else
				{
					return this.eye_green;
				}
			}
		}

		return this.blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		if (side == ForgeDirection.SOUTH.ordinal())
		{
			return this.eye_green;
		}

		return this.blockIcon;
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		world.setBlockMetadataWithNotify(x, y, z, side, 3);
		return true;
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

		if (tileEntity instanceof TileEntityDetector)
		{
			return ((TileEntityDetector) tileEntity).isPoweringTo(ForgeDirection.getOrientation(direction));
		}
		return 0;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int direction)
	{
		return isProvidingStrongPower(world, x, y, z, direction);
	}

	@Override
	public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
	{
		list.add(new Pair("ALDetector", TileEntityDetector.class));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{
		list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileEntityDetector.class, new RenderDetector()));
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityDetector();
	}

}

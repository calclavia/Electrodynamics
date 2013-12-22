package resonantinduction.furnace;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class BlockAdvancedFurnace extends BlockFurnace
{
	@SideOnly(Side.CLIENT)
	private Icon furnaceIconTop;
	@SideOnly(Side.CLIENT)
	private Icon furnaceIconFront;
	@SideOnly(Side.CLIENT)
	private Icon furnaceIconFrontBurn;

	protected BlockAdvancedFurnace(int id, boolean isBurning)
	{
		super(id, isBurning);
		this.setHardness(3.5F);
		this.setStepSound(soundStoneFootstep);
		this.setUnlocalizedName("furnace");

		if (isBurning)
		{
			this.setLightValue(0.875F);
		}
		else
		{
			this.setCreativeTab(CreativeTabs.tabDecorations);
		}
	}

	public static BlockAdvancedFurnace createNew(boolean isBurning)
	{
		int id = Block.furnaceIdle.blockID;

		if (isBurning)
		{
			id = Block.furnaceBurning.blockID;
		}

		Block.blocksList[id] = null;
		Item.itemsList[id] = null;
		return new BlockAdvancedFurnace(id, isBurning);
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon("furnace_side");
		this.furnaceIconFront = par1IconRegister.registerIcon("furnace_front_off");
		this.furnaceIconFrontBurn = par1IconRegister.registerIcon("furnace_front_on");
		this.furnaceIconTop = par1IconRegister.registerIcon("furnace_top");
	}

	@Override
	public Icon getBlockTexture(IBlockAccess access, int x, int y, int z, int side)
	{
		TileEntity tileEntity = (TileEntityAdvancedFurnace) access.getBlockTileEntity(x, y, z);
		int meta = access.getBlockMetadata(x, y, z);

		if (((TileEntityAdvancedFurnace) tileEntity).isBurning())
		{
			return side == 1 ? this.furnaceIconTop : (side == 0 ? this.furnaceIconTop : (side != meta ? this.blockIcon : this.furnaceIconFrontBurn));
		}
		else
		{
			return side == 1 ? this.furnaceIconTop : (side == 0 ? this.furnaceIconTop : (side != meta ? this.blockIcon : this.furnaceIconFront));
		}
	}

	@Override
	public Icon getIcon(int side, int meta)
	{
		return side == 1 ? this.furnaceIconTop : (side == 0 ? this.furnaceIconTop : (side != 3 ? this.blockIcon : this.furnaceIconFront));
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
	{
		TileEntity tileEntity = (TileEntityAdvancedFurnace) world.getBlockTileEntity(x, y, z);

		if (((TileEntityAdvancedFurnace) tileEntity).isBurning())
		{
			int l = world.getBlockMetadata(x, y, z);
			float f = (float) x + 0.5F;
			float f1 = (float) y + 0.0F + par5Random.nextFloat() * 6.0F / 16.0F;
			float f2 = (float) z + 0.5F;
			float f3 = 0.52F;
			float f4 = par5Random.nextFloat() * 0.6F - 0.3F;

			if (l == 4)
			{
				world.spawnParticle("smoke", (double) (f - f3), (double) f1, (double) (f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double) (f - f3), (double) f1, (double) (f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 5)
			{
				world.spawnParticle("smoke", (double) (f + f3), (double) f1, (double) (f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double) (f + f3), (double) f1, (double) (f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 2)
			{
				world.spawnParticle("smoke", (double) (f + f4), (double) f1, (double) (f2 - f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double) (f + f4), (double) f1, (double) (f2 - f3), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 3)
			{
				world.spawnParticle("smoke", (double) (f + f4), (double) f1, (double) (f2 + f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", (double) (f + f4), (double) f1, (double) (f2 + f3), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World par1World)
	{
		return new TileEntityAdvancedFurnace();
	}
}

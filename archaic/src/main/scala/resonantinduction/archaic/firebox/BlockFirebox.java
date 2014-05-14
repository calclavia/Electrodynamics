package resonantinduction.archaic.firebox;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonant.lib.prefab.block.BlockTile;
import resonant.lib.utility.FluidUtility;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFirebox extends BlockTile
{
	private Icon topOn;
	private Icon topOff;
	private Icon sideOn;
	private Icon sideOff;

	private Icon topElectricOn;
	private Icon topElectricOff;
	private Icon sideOnElectric;
	private Icon sideOffElectric;

	public BlockFirebox(int id)
	{
		super(id, Material.rock);
		setTickRandomly(true);
		setTextureName(Reference.PREFIX + "material_stone_slab");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconReg)
	{
		super.registerIcons(iconReg);
		sideOn = iconReg.registerIcon(Reference.PREFIX + "firebox_side_on");
		sideOff = iconReg.registerIcon(Reference.PREFIX + "firebox_side_off");
		topOn = iconReg.registerIcon(Reference.PREFIX + "firebox_top_on");
		topOff = iconReg.registerIcon(Reference.PREFIX + "firebox_top_off");

		sideOnElectric = iconReg.registerIcon(Reference.PREFIX + "firebox_electric_side_on");
		sideOffElectric = iconReg.registerIcon(Reference.PREFIX + "firebox_electric_side_off");
		topElectricOn = iconReg.registerIcon(Reference.PREFIX + "firebox_electric_top_on");
		topElectricOff = iconReg.registerIcon(Reference.PREFIX + "firebox_electric_top_off");

	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileFirebox)
		{
			TileFirebox tile = (TileFirebox) tileEntity;
			extractItem(tile, 0, player);
		}
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileFirebox)
		{
			TileFirebox tile = (TileFirebox) tileEntity;

			if (FluidUtility.playerActivatedFluidItem(world, x, y, z, player, side))
			{
				return true;
			}

			return interactCurrentItem(tile, 0, player);
		}

		return false;
	}

	@Override
	public Icon getBlockTexture(IBlockAccess access, int x, int y, int z, int side)
	{
		if (side == 0)
			return blockIcon;

		boolean isElectric = access.getBlockMetadata(x, y, z) == 1;
		boolean isBurning = false;
		TileEntity tile = access.getBlockTileEntity(x, y, z);

		if (tile instanceof TileFirebox)
		{
			isBurning = ((TileFirebox) tile).isBurning();
		}

		if (side == 1)
		{
			return isBurning ? (isElectric ? topElectricOn : topOn) : (isElectric ? topElectricOff : topOff);
		}

		return isBurning ? (isElectric ? sideOnElectric : sideOn) : (isElectric ? sideOffElectric : sideOff);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 0)
			return blockIcon;

		boolean isElectric = meta == 1;
		boolean isBurning = false;

		if (side == 1)
		{
			return isBurning ? (isElectric ? topElectricOn : topOn) : (isElectric ? topElectricOff : topOff);
		}

		return isBurning ? (isElectric ? sideOnElectric : sideOn) : (isElectric ? sideOffElectric : sideOff);
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (((TileFirebox) tileEntity).isBurning())
		{
			float xDisplace = x + 0.5F;
			float yDisplace = y + 0.0F + par5Random.nextFloat() * 6.0F / 16.0F;
			float zDisplace = z + 0.5F;
			float modifier = 0.52F;
			float randomValue = par5Random.nextFloat() * 0.6F - 0.3F;

			world.spawnParticle("smoke", xDisplace - modifier, yDisplace, zDisplace + randomValue, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", xDisplace - modifier, yDisplace, zDisplace + randomValue, 0.0D, 0.0D, 0.0D);

			world.spawnParticle("smoke", xDisplace + modifier, yDisplace, zDisplace + randomValue, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", xDisplace + modifier, yDisplace, zDisplace + randomValue, 0.0D, 0.0D, 0.0D);

			world.spawnParticle("smoke", xDisplace + randomValue, yDisplace, zDisplace - modifier, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", xDisplace + randomValue, yDisplace, zDisplace - modifier, 0.0D, 0.0D, 0.0D);

			world.spawnParticle("smoke", xDisplace + randomValue, yDisplace, zDisplace + modifier, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", xDisplace + randomValue, yDisplace, zDisplace + modifier, 0.0D, 0.0D, 0.0D);

			int blockIDAbove = world.getBlockId(x, y + 1, z);

			if (blockIDAbove == Block.waterStill.blockID)
			{
				for (int i = 0; i < 4; i++)
					world.spawnParticle("bubble", xDisplace + (par5Random.nextFloat() - 0.5), yDisplace + 1.5, zDisplace + (par5Random.nextFloat() - 0.5), 0.0D, 0.05D, 0.0D);
			}
		}
	}

	@Override
	public float getBlockBrightness(IBlockAccess access, int x, int y, int z)
	{
		TileEntity tileEntity = access.getBlockTileEntity(x, y, z);

		if (((TileFirebox) tileEntity).isBurning())
		{
			return 1;
		}

		return 0;
	}

	@Override
	public int damageDropped(int par1)
	{
		return par1;
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileFirebox();
	}
}

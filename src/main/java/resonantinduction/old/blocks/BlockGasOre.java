package resonantinduction.old.blocks;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialTransparent;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import resonantinduction.core.Settings;
import resonantinduction.old.Reference;
import resonantinduction.old.transport.fluid.EnumGas;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gas that is designed to generate underground in the same way as an ore
 * 
 * TODO code actual gas behavior such as expanding to fill an area but at the same time losing
 * volume
 * 
 * @author DarkGuardsman
 */
public class BlockGasOre extends Block implements IFluidBlock
{
	public static final int[] volumePerMeta = new int[] { 10, 35, 75, 125, 250, 500, 1000, 2000, 4000, 8000, 16000, 32000, 64000, 12800, 256000, 512000 };
	public static final Material gas = new MaterialTransparent(MapColor.airColor).setReplaceable();

	public BlockGasOre()
	{
		super(Settings.CONFIGURATION.getBlock("GasBlock", Settings.getNextBlockID()).getInt(), gas);
		this.setUnlocalizedName("BlockGas");
		this.setTickRandomly(true);
	}

	@Override
	public int tickRate(World par1World)
	{
		return 1;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		if (!world.isRemote)
		{

			final Vector3 vec = new Vector3(x, y, z);
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				int meta = world.getBlockMetadata(x, y, z);

				Vector3 sVec = vec.clone().modifyPositionFromSide(dir);
				int sMeta = sVec.getBlockMetadata(world);
				int blockID = sVec.getBlockID(world);
				Block block = Block.blocksList[blockID];

				if (blockID == 0 || block == null || block != null && block.isAirBlock(world, x, y, z) && blockID != this.blockID)
				{
					if (meta == 0)
					{
						world.setBlockToAir(x, y, z);
						break;
					}
					else
					{
						world.setBlock(x, y, z, this.blockID, meta / 2, 2);
						sVec.setBlock(world, this.blockID, meta / 2, 2);
						break;
					}
				}
				else if (blockID == this.blockID && meta > sMeta)
				{
					meta += sMeta;
					world.setBlock(x, y, z, this.blockID, meta / 2, 2);
					sVec.setBlock(world, this.blockID, meta / 2, 2);
					break;
				}
			}
		}
	}

	/* IFluidBlock */
	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain)
	{
		int meta = world.getBlockMetadata(x, y, z);
		FluidStack fluid = new FluidStack(EnumGas.NATURAL_GAS.getGas(), volumePerMeta[meta]);
		if (doDrain || fluid == null)
		{
			world.setBlockToAir(x, y, z);
		}
		return fluid;
	}

	@Override
	public boolean canDrain(World world, int x, int y, int z)
	{
		return false;
	}

	@Override
	public Fluid getFluid()
	{
		return EnumGas.NATURAL_GAS.getGas();
	}

	@Override
	public int idDropped(int par1, Random par2Random, int par3)
	{
		return 0;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}

	@Override
	public boolean isCollidable()
	{
		return false;
	}

	@Override
	public Icon getIcon(int par1, int par2)
	{
		return this.blockIcon;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(Reference.PREFIX + "gas");
	}

	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockColor()
	{
		return Color.yellow.getRGB();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int par1)
	{
		// TODO make the color darker as the meta value goes higher
		return this.getBlockColor();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z)
	{
		return this.getRenderColor(world.getBlockMetadata(x, y, z));
	}

	@Override
	public void getSubBlocks(int blockID, CreativeTabs tab, List creativeTabList)
	{
		creativeTabList.add(new ItemStack(blockID, 1, 15));
	}
}

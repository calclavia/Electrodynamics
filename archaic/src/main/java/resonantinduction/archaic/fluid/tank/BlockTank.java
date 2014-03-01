package resonantinduction.archaic.fluid.tank;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonantinduction.core.fluid.BlockFluidNetwork;
import resonantinduction.core.render.RIBlockRenderingHandler;
import resonantinduction.mechanical.fluid.pipe.ItemBlockFluidContainer;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTank extends BlockFluidNetwork
{
	public BlockTank(int id)
	{
		super(id, UniversalElectricity.machine);
		setHardness(1f);
		setResistance(5f);
	}

	@Override
	public boolean isOpaqueCube()
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
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return RIBlockRenderingHandler.ID;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		int checkBlockID = blockAccess.getBlockId(x, y, z);
		return super.shouldSideBeRendered(blockAccess, x, y, z, side);
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			return FluidUtility.playerActivatedFluidItem(world, x, y, z, entityplayer, side);
		}
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileTank();
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int par5)
	{
		TileTank tileEntity = (TileTank) world.getBlockTileEntity(x, y, z);
		if (tileEntity != null && tileEntity.getNetwork().getTank().getFluid() != null)
		{
			return 15 * (tileEntity.getNetwork().getTank().getFluidAmount() / tileEntity.getNetwork().getTank().getCapacity());
		}
		return 0;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return new ItemStack(this, 1, 0);
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity instanceof TileTank)
		{
			ret.add(new ItemStack(this, 1, 0));
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(this, 1, 0));

	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			ItemStack dropStack = ItemBlockFluidContainer.getWrenchedItem(world, new Vector3(x, y, z));
			if (dropStack != null)
			{
				if (entityPlayer.getHeldItem() == null)
				{
					entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, dropStack);
				}
				else
				{
					InventoryUtility.dropItemStack(world, new Vector3(x, y, z), dropStack);
				}
				world.setBlockToAir(x, y, z);
			}
		}
		return true;
	}
}

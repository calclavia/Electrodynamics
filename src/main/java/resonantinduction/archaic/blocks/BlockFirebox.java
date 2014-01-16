package resonantinduction.archaic.blocks;

import java.util.Random;

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
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFirebox extends BlockRI
{
	private Icon topOn;
	private Icon topOff;

	public BlockFirebox()
	{
		super("firebox", Material.piston);
		this.setTickRandomly(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconReg)
	{
		super.registerIcons(iconReg);
		topOn = iconReg.registerIcon(Reference.PREFIX + "firebox_top_on");
		topOff = iconReg.registerIcon(Reference.PREFIX + "firebox_top_off");
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileFirebox)
		{
			TileFirebox tile = (TileFirebox) tileEntity;

			if (tile.getStackInSlot(0) == null && entityPlayer.inventory.getCurrentItem() != null)
			{
				tile.setInventorySlotContents(0, entityPlayer.inventory.getCurrentItem());
				entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, null);
			}
			else
			{
				
			}
		}

		return false;
	}

	@Override
	public Icon getBlockTexture(IBlockAccess access, int x, int y, int z, int side)
	{
		TileEntity tile = access.getBlockTileEntity(x, y, z);

		if (tile instanceof TileFirebox)
		{
			if (access.getBlockMetadata(x, y, z) == side)
			{
				if (((TileFirebox) tile).isBurning())
				{
					return topOn;
				}
				else
				{
					return topOff;
				}
			}
		}

		return blockIcon;
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (((TileFirebox) tileEntity).isBurning())
		{
			int l = world.getBlockMetadata(x, y, z);
			float f = x + 0.5F;
			float f1 = y + 0.0F + par5Random.nextFloat() * 6.0F / 16.0F;
			float f2 = z + 0.5F;
			float f3 = 0.52F;
			float f4 = par5Random.nextFloat() * 0.6F - 0.3F;

			if (l == 4)
			{
				world.spawnParticle("smoke", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
			}
			else if (l == 5)
			{
				world.spawnParticle("smoke", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
			}
			else if (l == 2)
			{
				world.spawnParticle("smoke", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
			}
			else if (l == 3)
			{
				world.spawnParticle("smoke", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
				world.spawnParticle("flame", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == meta)
		{
			return topOff;
		}

		return this.blockIcon;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack stack)
	{
		int angle = MathHelper.floor_double((entityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int change = 3;

		switch (angle)
		{
			case 0:
				change = 2;
				break;

			case 1:
				change = 5;
				break;

			case 2:
				change = 3;
				break;

			case 3:
				change = 4;
				break;
		}

		world.setBlockMetadataWithNotify(x, y, z, change, 3);
		world.scheduleBlockUpdate(x, y, z, blockID, 20);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int side)
	{
		world.scheduleBlockUpdate(x, y, z, blockID, 20);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileFirebox();
	}
}

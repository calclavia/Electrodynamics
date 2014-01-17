package resonantinduction.archaic.firebox;

import java.util.Random;

import codechicken.multipart.ControlKeyModifer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRI;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFirebox extends BlockRI
{
	private Icon topOn;
	private Icon topOff;

	public BlockFirebox()
	{
		super("firebox", Material.wood);
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
			return interactCurrentItem(tile, 0, player);
		}

		return false;
	}

	@Override
	public Icon getBlockTexture(IBlockAccess access, int x, int y, int z, int side)
	{
		TileEntity tile = access.getBlockTileEntity(x, y, z);

		if (tile instanceof TileFirebox)
		{
			if (side == 1)
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
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 1)
		{
			return topOff;
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

			world.spawnParticle("smoke", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);

			world.spawnParticle("smoke", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);

			world.spawnParticle("smoke", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);

			world.spawnParticle("smoke", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public boolean isControlDown(EntityPlayer player)
	{
		return ControlKeyModifer.isControlDown(player);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileFirebox();
	}
}

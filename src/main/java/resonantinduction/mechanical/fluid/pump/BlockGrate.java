package resonantinduction.mechanical.fluid.pump;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRIRotatable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGrate extends BlockRIRotatable
{
	private Icon drainIcon;
	private Icon fillIcon;

	public BlockGrate()
	{
		super("grate");
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileGrate();
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		this.drainIcon = iconRegister.registerIcon(Reference.PREFIX + "grate_drain");
		this.fillIcon = iconRegister.registerIcon(Reference.PREFIX + "grate_fill");
		super.registerIcons(iconRegister);
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
	public Icon getIcon(int side, int metadata)
	{
		if (side == metadata)
		{
			return this.drainIcon;
		}

		return this.blockIcon;
	}

	@Override
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
	{
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		ForgeDirection dir = ForgeDirection.getOrientation(side);

		if (entity instanceof TileGrate)
		{
			if (dir == ((TileGrate) entity).getDirection())
			{
				if (((TileGrate) entity).canDrain())
				{
					return this.drainIcon;
				}
				else
				{
					return this.fillIcon;
				}
			}
		}

		return this.blockIcon;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase p, ItemStack itemStack)
	{
		int angle = MathHelper.floor_double((p.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		world.setBlockMetadataWithNotify(x, y, z, angle, 3);
		TileEntity entity = world.getBlockTileEntity(x, y, z);
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			int meta = world.getBlockMetadata(x, y, z);

			if (world.getBlockMetadata(x, y, z) < 6)
			{
				meta += 6;
			}
			else
			{
				meta -= 6;
			}

			world.setBlockMetadataWithNotify(x, y, z, meta, 3);
			TileEntity entity = world.getBlockTileEntity(x, y, z);

			if (entity instanceof TileGrate)
			{
				entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Draining Sources? " + ((TileGrate) entity).canDrain()));

			}
			return true;
		}
		return true;
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			int meta = side;
			if (world.getBlockMetadata(x, y, z) > 5)
			{
				meta += 6;
			}
			world.setBlockMetadataWithNotify(x, y, z, meta, 3);
			return true;
		}
		return true;
	}
}

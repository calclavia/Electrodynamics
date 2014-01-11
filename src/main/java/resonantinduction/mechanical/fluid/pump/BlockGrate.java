package resonantinduction.mechanical.fluid.pump;

import java.util.Set;

import net.minecraft.block.material.Material;
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
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.block.BlockRI;

import com.builtbroken.common.Pair;

public class BlockGrate extends BlockRI
{
	private Icon blockIcon;
	private Icon drainIcon;
	private Icon fillIcon;

	public BlockGrate()
	{
		super("grate", Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileGrate();
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(Reference.PREFIX + "ironMachineSide");
		this.drainIcon = par1IconRegister.registerIcon(Reference.PREFIX + "drain");
		this.fillIcon = par1IconRegister.registerIcon(Reference.PREFIX + "drain2");
	}

	@Override
	public Icon getIcon(int par1, int par2)
	{
		return par1 != 1 && par1 != 0 ? this.blockIcon : this.drainIcon;
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

package resonantinduction.mechanical.energy.turbine;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import calclavia.lib.prefab.turbine.BlockTurbine;
import calclavia.lib.prefab.turbine.TileTurbine;

public class BlockMechanicalTurbine extends BlockTurbine
{
	public BlockMechanicalTurbine(int id)
	{
		super(id, Material.iron);
		setTextureName(Reference.PREFIX + "material_wood_surface");
		rotationMask = Byte.parseByte("111111", 2);
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileTurbine)
			return ((TileTurbine) tile).tier;

		return 0;
	}

	/**
	 * Temporarily "cheat" var for dropping with damage.
	 */
	int dropDamage = 0;

	@Override
	public int damageDropped(int par1)
	{
		return dropDamage;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		dropDamage = getDamageValue(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack)
	{
		super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMechanicalTurbine)
		{
			((TileMechanicalTurbine) tileEntity).tier = itemStack.getItemDamage();
		}
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileTurbine)
		{
			if (!world.isRemote)
			{
				TileTurbine tile = (TileTurbine) tileEntity;

				if (tile.getMultiBlock().isConstructed())
				{
					tile.getMultiBlock().deconstruct();
					tile.multiBlockRadius++;

					if (!tile.getMultiBlock().construct())
					{
						tile.multiBlockRadius = 1;
					}

					return true;
				}
				else
				{
					if (!tile.getMultiBlock().construct())
					{
						tile.multiBlockRadius = 1;
						tile.getMultiBlock().construct();
					}
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileTurbine)
		{
			if (!world.isRemote && !((TileTurbine) tileEntity).getMultiBlock().isConstructed())
			{
				if (side == ((TileTurbine) tileEntity).getDirection().ordinal())
					world.setBlockMetadataWithNotify(x, y, z, side ^ 1, 3);
				else
					world.setBlockMetadataWithNotify(x, y, z, side, 3);
			}
		}

		return true;
	}

}

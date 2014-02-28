package resonantinduction.mechanical.energy.turbine;

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
				return ((TileTurbine) tileEntity).getMultiBlock().toggleConstruct();
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (entityPlayer.getCurrentEquippedItem() == null)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof TileTurbine)
			{
				if (!world.isRemote && !((TileTurbine) tileEntity).getMultiBlock().isConstructed())
				{
					((TileTurbine) tileEntity).multiBlockRadius = Math.max(((TileTurbine) tileEntity).multiBlockRadius + 1, 1);
					entityPlayer.addChatMessage("Turbine radius: " + ((TileTurbine) tileEntity).multiBlockRadius);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileTurbine)
		{
			if (!world.isRemote && !((TileTurbine) tileEntity).getMultiBlock().isConstructed())
			{
				((TileTurbine) tileEntity).multiBlockRadius = Math.max(((TileTurbine) tileEntity).multiBlockRadius - 1, 1);
				entityPlayer.addChatMessage("Turbine radius: " + ((TileTurbine) tileEntity).multiBlockRadius);
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

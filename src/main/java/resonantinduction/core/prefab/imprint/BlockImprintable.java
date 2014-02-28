package resonantinduction.core.prefab.imprint;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import resonantinduction.api.IFilterable;
import calclavia.lib.prefab.block.BlockRotatable;

/**
 * Extend this block class if a filter is allowed to be placed inside of this block.
 * 
 * @author Calclavia
 */
public abstract class BlockImprintable extends BlockRotatable
{
	public BlockImprintable(int id, Material material)
	{
		super(id, material);
	}

	/** Allows filters to be placed inside of this block. */
	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IFilterable)
		{
			ItemStack containingStack = ((IFilterable) tileEntity).getFilter();

			if (containingStack != null)
			{
				if (!world.isRemote)
				{
					EntityItem dropStack = new EntityItem(world, player.posX, player.posY, player.posZ, containingStack);
					dropStack.delayBeforeCanPickup = 0;
					world.spawnEntityInWorld(dropStack);
				}

				((IFilterable) tileEntity).setFilter(null);
				return true;
			}
			else
			{
				if (player.getCurrentEquippedItem() != null)
				{
					if (player.getCurrentEquippedItem().getItem() instanceof ItemImprint)
					{
						((IFilterable) tileEntity).setFilter(player.getCurrentEquippedItem());
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
						return true;
					}
				}
			}

		}

		return false;
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			if (tileEntity instanceof TileFilterable)
			{
				((TileFilterable) tileEntity).toggleInversion();
				world.markBlockForRenderUpdate(x, y, z);
				world.markBlockForUpdate(x, y, z);
			}
		}

		return true;
	}
}

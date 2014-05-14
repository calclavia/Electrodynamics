package resonantinduction.core.prefab.imprint;

import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.IFilterable;
import resonant.api.IRotatable;
import resonant.lib.content.module.prefab.TileInventory;
import universalelectricity.api.vector.Vector3;

public abstract class TileFilterable extends TileInventory implements IRotatable, IFilterable
{
	private ItemStack filterItem;
	private boolean inverted;
	public static final int FILTER_SLOT = 0;
	public static final int BATERY_DRAIN_SLOT = 1;

	public TileFilterable()
	{
		super(null);
		this.maxSlots = 2;
	}

	public TileFilterable(Material material)
	{
		super(material);
		this.maxSlots = 2;
	}

	protected boolean isFunctioning()
	{
		return true;
	}

	/** Allows filters to be placed inside of this block. */
	@Override
	public boolean use(EntityPlayer player, int side, Vector3 hit)
	{
		ItemStack containingStack = getFilter();

		if (containingStack != null)
		{
			if (!world().isRemote)
			{
				EntityItem dropStack = new EntityItem(world(), player.posX, player.posY, player.posZ, containingStack);
				dropStack.delayBeforeCanPickup = 0;
				world().spawnEntityInWorld(dropStack);
			}

			setFilter(null);
			return true;
		}
		else
		{
			if (player.getCurrentEquippedItem() != null)
			{
				if (player.getCurrentEquippedItem().getItem() instanceof ItemImprint)
				{
					setFilter(player.getCurrentEquippedItem());
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean configure(EntityPlayer player, int side, Vector3 hit)
	{
		toggleInversion();
		markUpdate();
		markRender();
		return true;
	}

	/**
	 * Looks through the things in the filter and finds out which item is being filtered.
	 * 
	 * @return Is this filterable block filtering this specific ItemStack?
	 */
	public boolean isFiltering(ItemStack itemStack)
	{
		if (this.getFilter() != null && itemStack != null)
		{
			Set<ItemStack> checkStacks = ItemImprint.getFilters(getFilter());

			if (checkStacks != null)
			{
				for (ItemStack stack : checkStacks)
				{
					if (stack.isItemEqual(itemStack))
					{
						return !inverted;
					}
				}
			}
		}

		return inverted;
	}

	@Override
	public void setFilter(ItemStack filter)
	{
		this.filterItem = filter;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public ItemStack getFilter()
	{
		return this.filterItem;
	}

	public void setInverted(boolean inverted)
	{
		this.inverted = inverted;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public boolean isInverted()
	{
		return this.inverted;
	}

	public void toggleInversion()
	{
		setInverted(!isInverted());
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(getBlockType() != null ? getBlockMetadata() : 0);
	}

	@Override
	public void setDirection(ForgeDirection facingDirection)
	{
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, facingDirection.ordinal(), 3);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("inverted", inverted);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("filter"))
		{
			this.getInventory().setInventorySlotContents(0, ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("filter")));
		}
		inverted = nbt.getBoolean("inverted");
	}

}

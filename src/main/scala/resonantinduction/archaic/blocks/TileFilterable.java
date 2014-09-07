package resonantinduction.archaic.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.IFilterable;
import resonant.api.IRotatable;
import resonant.lib.content.prefab.java.TileInventory;
import universalelectricity.core.transform.vector.Vector3;

import java.util.Set;

public abstract class TileFilterable extends TileInventory implements IRotatable, IFilterable
{
	public static final int FILTER_SLOT = 0;
	public static final int BATERY_DRAIN_SLOT = 1;
	private ItemStack filterItem;
	private boolean inverted;

	public TileFilterable()
	{
		super(Material.wood);
		this.setSizeInventory(2);
	}

	public TileFilterable(Material material)
	{
		super(material);
		this.setSizeInventory(2);
	}

	protected boolean isFunctioning()
	{
		return true;
	}

	/**
	 * Allows filters to be placed inside of this block.
	 */
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
	public ItemStack getFilter()
	{
		return this.filterItem;
	}

	@Override
	public void setFilter(ItemStack filter)
	{
		this.filterItem = filter;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public boolean isInverted()
	{
		return this.inverted;
	}

	public void setInverted(boolean inverted)
	{
		this.inverted = inverted;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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

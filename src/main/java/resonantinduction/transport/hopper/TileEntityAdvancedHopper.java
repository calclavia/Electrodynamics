package resonantinduction.transport.hopper;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.transport.imprinter.ItemImprinter;
import resonantinduction.transport.imprinter.TileEntityFilterable;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.InvInteractionHelper;

import com.builtbroken.minecraft.helpers.HelperMethods;

/**
 * Advanced version of the hopper with features such as redstone control, sorting, filtering, and
 * crate version.
 * 
 * @author DarkGuardsman
 */
public class TileEntityAdvancedHopper extends TileEntityFilterable
{

	public ForgeDirection connection = ForgeDirection.DOWN;
	public boolean[] connections = new boolean[6];
	public boolean singleConnection = true;

	/** The class that interacts with inventories for this machine */
	private InvInteractionHelper invExtractionHelper;

	public TileEntityAdvancedHopper()
	{
		this.invSlots = 5;
	}

	/** Gets the class that managed extracting and placing items into inventories */
	public InvInteractionHelper invHelper()
	{
		if (invExtractionHelper == null || invExtractionHelper.world != this.worldObj)
		{
			this.invExtractionHelper = new InvInteractionHelper(this.worldObj, new Vector3(this), this.getFilter() != null ? ItemImprinter.getFilters(getFilter()) : null, this.isInverted());
		}
		return invExtractionHelper;
	}

	/**
	 * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner
	 * uses this to count ticks and creates a new spawn inside its implementation.
	 */
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (!this.worldObj.isRemote && this.ticks % 8 == 0)
		{
			this.grabItems();
			this.dumpItems();
		}
	}

	public void grabItems()
	{
		Vector3 inputPos = this.getThisPos().clone().modifyPositionFromSide(ForgeDirection.UP);
		List<EntityItem> itemsInBound = HelperMethods.getEntitiesInDirection(worldObj, this.getThisPos(), ForgeDirection.UP);
		ItemStack itemStack = invHelper().tryGrabFromPosition(inputPos, ForgeDirection.UP, 1);
		if (itemStack != null)
		{
			itemStack = invHelper().tryPlaceInPosition(itemStack, this.getThisPos(), ForgeDirection.UNKNOWN);
			if (itemStack != null)
			{
				itemStack = invHelper().tryPlaceInPosition(itemStack, inputPos, ForgeDirection.DOWN);
				if (itemStack != null)
				{
					invHelper().throwItem(inputPos, itemStack);
				}
			}
		}
		else if (inputPos.getTileEntity(worldObj) == null && itemsInBound != null)
		{
			for (EntityItem entity : itemsInBound)
			{
				if (entity.isDead)
				{
					continue;
				}
				ItemStack remainingStack = entity.getEntityItem().copy();
				if (this.getFilter() == null || this.isFiltering(remainingStack))
				{
					if (remainingStack != null)
					{
						remainingStack = invHelper().tryPlaceInPosition(itemStack, this.getThisPos(), ForgeDirection.UNKNOWN);
					}
					if (remainingStack == null || remainingStack.stackSize <= 0)
					{
						entity.setDead();
					}
					else
					{
						entity.setEntityItemStack(remainingStack);
					}
				}
			}
		}
	}

	public void dumpItems()
	{
		Vector3 outputPos = this.getThisPos().clone().modifyPositionFromSide(this.connection);
		for (int slot = 0; slot < this.getInventory().getSizeInventory(); slot++)
		{
			if (this.getInventory().getStackInSlot(slot) != null)
			{
				ItemStack stack = this.getInventory().getStackInSlot(slot).copy();
				if (this.getFilter() == null || this.isFiltering(stack))
				{
					stack = this.invHelper().tryGrabFromPosition(outputPos, this.connection.getOpposite(), 1);
					if (stack == null || !areItemStacksEqualItem(stack, this.getInventory().getStackInSlot(slot)) || this.getInventory().getStackInSlot(slot).stackSize != stack.stackSize)
					{
						this.getInventory().setInventorySlotContents(slot, stack);
						this.onInventoryChanged();
					}
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
	}

	@Override
	public String getInvName()
	{
		return "container.advancedhopper";
	}

	private static boolean areItemStacksEqualItem(ItemStack stack, ItemStack stack2)
	{
		return stack.itemID != stack2.itemID ? false : (stack.getItemDamage() != stack2.getItemDamage() ? false : (stack.stackSize > stack.getMaxStackSize() ? false : ItemStack.areItemStackTagsEqual(stack, stack2)));
	}
}

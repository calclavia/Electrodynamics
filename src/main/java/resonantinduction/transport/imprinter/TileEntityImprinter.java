package resonantinduction.transport.imprinter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import resonantinduction.api.AutoCraftEvent;
import resonantinduction.api.IArmbot;
import resonantinduction.api.IArmbotUseable;
import resonantinduction.api.coding.args.ArgumentData;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.AutoCraftingManager;
import calclavia.lib.utility.AutoCraftingManager.IAutoCrafter;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.prefab.invgui.ISlotPickResult;

public class TileEntityImprinter extends TileAdvanced implements ISidedInventory, IArmbotUseable, ISlotPickResult, IAutoCrafter
{
	public static final int IMPRINTER_MATRIX_START = 9;
	public static final int INVENTORY_START = IMPRINTER_MATRIX_START + 3;

	private AutoCraftingManager craftManager;
	/** 9 slots for crafting, 1 slot for an imprint, 1 slot for an item */
	public ItemStack[] craftingMatrix = new ItemStack[9];
	public static final int[] craftingSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };

	public ItemStack[] imprinterMatrix = new ItemStack[3];
	public static final int[] imprinterSlots = { IMPRINTER_MATRIX_START, IMPRINTER_MATRIX_START + 1, IMPRINTER_MATRIX_START + 2 };
	int imprintInputSlot = 0;
	int imprintOutputSlot = 1;
	int craftingOutputSlot = 2;

	/** The Imprinter inventory containing slots. */
	public ItemStack[] containingItems = new ItemStack[18];
	public static int[] inventorySlots;

	/** The containing currently used by the imprinter. */
	public ContainerImprinter container;

	/** Is the current crafting result a result of an imprint? */
	private boolean isImprinting = false;

	/** The ability for the imprinter to serach nearby inventories. */
	public boolean searchInventories = true;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	/** Gets the AutoCraftingManager that does all the crafting results */
	public AutoCraftingManager getCraftingManager()
	{
		if (craftManager == null)
		{
			craftManager = new AutoCraftingManager(this);
		}
		return craftManager;
	}

	@Override
	public int getSizeInventory()
	{
		return this.craftingMatrix.length + this.imprinterMatrix.length + this.containingItems.length;
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor
	 * sections).
	 */
	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack)
	{
		if (slot < this.getSizeInventory())
		{
			if (slot < IMPRINTER_MATRIX_START)
			{
				this.craftingMatrix[slot] = itemStack;
			}
			else if (slot < INVENTORY_START)
			{
				this.imprinterMatrix[slot - IMPRINTER_MATRIX_START] = itemStack;
			}
			else
			{
				this.containingItems[slot - INVENTORY_START] = itemStack;
			}
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int amount)
	{
		if (this.getStackInSlot(i) != null)
		{
			ItemStack stack;

			if (this.getStackInSlot(i).stackSize <= amount)
			{
				stack = this.getStackInSlot(i);
				this.setInventorySlotContents(i, null);
				return stack;
			}
			else
			{
				stack = this.getStackInSlot(i).splitStack(amount);

				if (this.getStackInSlot(i).stackSize == 0)
				{
					this.setInventorySlotContents(i, null);
				}

				return stack;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if (slot < IMPRINTER_MATRIX_START)
		{
			return this.craftingMatrix[slot];
		}
		else if (slot < INVENTORY_START)
		{
			return this.imprinterMatrix[slot - IMPRINTER_MATRIX_START];
		}
		else
		{
			return this.containingItems[slot - INVENTORY_START];
		}
	}

	/**
	 * When some containers are closed they call this on each slot, then drop whatever it returns as
	 * an EntityItem - like when you close a workbench GUI.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if (this.getStackInSlot(slot) != null)
		{
			ItemStack var2 = this.getStackInSlot(slot);
			this.setInventorySlotContents(slot, null);
			return var2;
		}
		else
		{
			return null;
		}
	}

	@Override
	public String getInvName()
	{
		return TranslationUtility.getLocal("tile.imprinter.name");
	}

	@Override
	public void openChest()
	{
		this.onInventoryChanged();
	}

	@Override
	public void closeChest()
	{
		this.onInventoryChanged();
	}

	/**
	 * Construct an InventoryCrafting Matrix on the fly.
	 * 
	 * @return
	 */
	public InventoryCrafting getCraftingMatrix()
	{
		if (this.container != null)
		{
			InventoryCrafting inventoryCrafting = new InventoryCrafting(this.container, 3, 3);

			for (int i = 0; i < this.craftingMatrix.length; i++)
			{
				inventoryCrafting.setInventorySlotContents(i, this.craftingMatrix[i]);
			}

			return inventoryCrafting;
		}

		return null;
	}

	public void replaceCraftingMatrix(InventoryCrafting inventoryCrafting)
	{
		for (int i = 0; i < this.craftingMatrix.length; i++)
		{
			this.craftingMatrix[i] = inventoryCrafting.getStackInSlot(i);
		}
	}

	public boolean isMatrixEmpty()
	{
		for (int i = 0; i < 9; i++)
		{
			if (this.craftingMatrix[i] != null)
				return false;
		}

		return true;
	}

	/** Updates all the output slots. Call this to update the Imprinter. */
	@Override
	public void onInventoryChanged()
	{
		if (!this.worldObj.isRemote)
		{
			/** Makes the stamping recipe for filters */
			this.isImprinting = false;

			if (this.isMatrixEmpty() && this.imprinterMatrix[imprintInputSlot] != null && this.imprinterMatrix[1] != null)
			{
				if (this.imprinterMatrix[imprintInputSlot].getItem() instanceof ItemImprinter)
				{
					ItemStack outputStack = this.imprinterMatrix[imprintInputSlot].copy();
					outputStack.stackSize = 1;
					ArrayList<ItemStack> filters = ItemImprinter.getFilters(outputStack);
					boolean filteringItemExists = false;

					for (ItemStack filteredStack : filters)
					{
						if (filteredStack.isItemEqual(this.imprinterMatrix[imprintOutputSlot]))
						{
							filters.remove(filteredStack);
							filteringItemExists = true;
							break;
						}
					}

					if (!filteringItemExists)
					{
						filters.add(this.imprinterMatrix[imprintOutputSlot]);
					}

					ItemImprinter.setFilters(outputStack, filters);
					this.imprinterMatrix[craftingOutputSlot] = outputStack;
					this.isImprinting = true;
				}
			}

			if (!this.isImprinting)
			{
				this.imprinterMatrix[craftingOutputSlot] = null;

				/** Try to craft from crafting grid. If not possible, then craft from imprint. */
				boolean didCraft = false;

				/** Simulate an Inventory Crafting Instance */
				InventoryCrafting inventoryCrafting = this.getCraftingMatrix();

				if (inventoryCrafting != null)
				{
					ItemStack matrixOutput = CraftingManager.getInstance().findMatchingRecipe(inventoryCrafting, this.worldObj);

					if (matrixOutput != null && this.getCraftingManager().getIdealRecipe(matrixOutput) != null)
					{
						this.imprinterMatrix[craftingOutputSlot] = matrixOutput;
						didCraft = true;
					}
				}

				if (this.imprinterMatrix[imprintInputSlot] != null && !didCraft)
				{
					if (this.imprinterMatrix[imprintInputSlot].getItem() instanceof ItemImprinter)
					{

						ArrayList<ItemStack> filters = ItemImprinter.getFilters(this.imprinterMatrix[0]);

						for (ItemStack outputStack : filters)
						{
							if (outputStack != null)
							{
								// System.out.println("Imprint: Geting recipe for " +
								// outputStack.toString());
								Pair<ItemStack, ItemStack[]> idealRecipe = this.getCraftingManager().getIdealRecipe(outputStack);

								if (idealRecipe != null)
								{
									// System.out.println("Imprint: found ideal recipe for  " +
									// idealRecipe.getKey().toString());
									ItemStack recipeOutput = idealRecipe.left();
									if (recipeOutput != null & recipeOutput.stackSize > 0)
									{
										this.imprinterMatrix[craftingOutputSlot] = recipeOutput;
										didCraft = true;
										break;
									}
								}
							}
						}
					}
				}

				if (!didCraft)
				{
					this.imprinterMatrix[craftingOutputSlot] = null;
				}
			}
		}
	}

	@Override
	public void onPickUpFromSlot(EntityPlayer entityPlayer, int s, ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (this.isImprinting)
			{
				this.imprinterMatrix[0] = null;
			}
			else
			{
				Pair<ItemStack, ItemStack[]> idealRecipeItem = this.getCraftingManager().getIdealRecipe(itemStack);

				if (idealRecipeItem != null)
				{
					this.getCraftingManager().consumeItems(idealRecipeItem.right().clone());
				}
			}
		}
	}

	/** Tries to let the Armbot craft an item. */
	@Override
	public boolean onUse(IArmbot armbot, List<ArgumentData> data)
	{
		this.onInventoryChanged();

		if (this.imprinterMatrix[craftingOutputSlot] != null)
		{
			AutoCraftEvent.PreCraft event = new AutoCraftEvent.PreCraft(this.worldObj, new Vector3(this), this, this.imprinterMatrix[craftingOutputSlot]);
			if (!event.isCanceled())
			{
				armbot.grabObject(this.imprinterMatrix[craftingOutputSlot].copy());
				this.onPickUpFromSlot(null, 2, this.imprinterMatrix[craftingOutputSlot]);
				this.imprinterMatrix[craftingOutputSlot] = null;
				return true;
			}
		}

		return false;
	}

	// ///////////////////////////////////////
	// // Save And Data processing //////
	// ///////////////////////////////////////

	/** NBT Data */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		NBTTagList var2 = nbt.getTagList("Items");
		this.craftingMatrix = new ItemStack[9];
		this.imprinterMatrix = new ItemStack[3];
		this.containingItems = new ItemStack[18];

		for (int i = 0; i < var2.tagCount(); ++i)
		{
			NBTTagCompound var4 = (NBTTagCompound) var2.tagAt(i);
			byte var5 = var4.getByte("Slot");

			if (var5 >= 0 && var5 < this.getSizeInventory())
			{
				this.setInventorySlotContents(var5, ItemStack.loadItemStackFromNBT(var4));
			}
		}

		this.searchInventories = nbt.getBoolean("searchInventories");
	}

	/** Writes a tile entity to NBT. */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		NBTTagList var2 = new NBTTagList();

		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			if (this.getStackInSlot(i) != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) i);
				this.getStackInSlot(i).writeToNBT(var4);
				var2.appendTag(var4);
			}
		}

		nbt.setTag("Items", var2);

		nbt.setBoolean("searchInventories", this.searchInventories);
	}

	// ///////////////////////////////////////
	// // Inventory Access side Methods //////
	// ///////////////////////////////////////

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : entityplayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;

	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return this.getCraftingInv();
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side)
	{
		return this.isItemValidForSlot(slot, itemstack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side)
	{
		return this.isItemValidForSlot(slot, itemstack);
	}

	@Override
	public int[] getCraftingInv()
	{
		if (TileEntityImprinter.inventorySlots == null)
		{
			TileEntityImprinter.inventorySlots = new int[18];
			for (int i = 0; i < inventorySlots.length; i++)
			{
				inventorySlots[i] = TileEntityImprinter.INVENTORY_START + i;
			}
		}
		return TileEntityImprinter.inventorySlots;
	}
}

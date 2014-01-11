package resonantinduction.archaic.engineering;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import resonantinduction.api.IArmbot;
import resonantinduction.api.IArmbotUseable;
import resonantinduction.api.events.AutoCraftEvent;
import resonantinduction.archaic.imprint.ItemBlockFilter;
import resonantinduction.archaic.imprint.TileImprinter;
import resonantinduction.electrical.encoder.coding.args.ArgumentData;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.slot.ISlotPickResult;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.AutoCraftingManager;
import calclavia.lib.utility.AutoCraftingManager.IAutoCrafter;
import calclavia.lib.utility.LanguageUtility;

import com.builtbroken.common.Pair;

public class TileEngineeringTable extends TileAdvanced implements ISidedInventory, IArmbotUseable, ISlotPickResult, IAutoCrafter
{
	public static final int CRAFTING_MATRIX_END = 9;

	private AutoCraftingManager craftManager;

	/** 9 slots for crafting, 1 slot for a output. */
	public ItemStack[] craftingMatrix = new ItemStack[9];
	public static final int[] craftingSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };

	/** The output inventory containing slots. */
	public ItemStack[] output = new ItemStack[1];
	public final int craftingOutputSlot = 0;
	public static int[] inventorySlots;

	/** The containing currently used by the imprinter. */
	public ContainerEngineering container;

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
		return 10;
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
			if (slot < CRAFTING_MATRIX_END)
			{
				this.craftingMatrix[slot] = itemStack;
			}
			else
			{
				this.output[slot - CRAFTING_MATRIX_END] = itemStack;
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
		if (slot < CRAFTING_MATRIX_END)
		{
			return this.craftingMatrix[slot];
		}
		else
		{
			return this.output[slot - CRAFTING_MATRIX_END];
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
		return LanguageUtility.getLocal("tile.imprinter.name");
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

	/** Updates all the output slots. Call this to update the Engineering Table. */
	@Override
	public void onInventoryChanged()
	{
		if (!this.worldObj.isRemote)
		{
			this.output[craftingOutputSlot] = null;

			/** Try to craft from crafting grid. If not possible, then craft from imprint. */
			boolean didCraft = false;

			/** Simulate an Inventory Crafting Instance */
			InventoryCrafting inventoryCrafting = this.getCraftingMatrix();

			if (inventoryCrafting != null)
			{
				ItemStack matrixOutput = CraftingManager.getInstance().findMatchingRecipe(inventoryCrafting, this.worldObj);

				if (matrixOutput != null && this.getCraftingManager().getIdealRecipe(matrixOutput) != null)
				{
					this.output[craftingOutputSlot] = matrixOutput;
					didCraft = true;
				}
			}

			/*
			if (this.output[imprintInputSlot] != null && !didCraft)
			{
				if (this.output[imprintInputSlot].getItem() instanceof ItemBlockFilter)
				{
					ArrayList<ItemStack> filters = ItemBlockFilter.getFilters(this.output[0]);

					for (ItemStack outputStack : filters)
					{
						if (outputStack != null)
						{
							Pair<ItemStack, ItemStack[]> idealRecipe = this.getCraftingManager().getIdealRecipe(outputStack);

							if (idealRecipe != null)
							{
								ItemStack recipeOutput = idealRecipe.left();
								if (recipeOutput != null & recipeOutput.stackSize > 0)
								{
									this.output[craftingOutputSlot] = recipeOutput;
									didCraft = true;
									break;
								}
							}
						}
					}
				}
			}*/
		}
	}

	@Override
	public void onPickUpFromSlot(EntityPlayer entityPlayer, int s, ItemStack itemStack)
	{
		if (itemStack != null)
		{

			Pair<ItemStack, ItemStack[]> idealRecipeItem = this.getCraftingManager().getIdealRecipe(itemStack);

			if (idealRecipeItem != null)
			{
				this.getCraftingManager().consumeItems(idealRecipeItem.right().clone());
			}

		}
	}

	/** Tries to let the Armbot craft an item. */
	@Override
	public boolean onUse(IArmbot armbot, List<ArgumentData> data)
	{
		this.onInventoryChanged();

		/*
		if (this.imprinterMatrix[craftingOutputSlot] != null)
		{
			AutoCraftEvent.PreCraft event = new AutoCraftEvent.PreCraft(this.worldObj, new Vector3(this), this, this.imprinterMatrix[craftingOutputSlot]);
			MinecraftForge.EVENT_BUS.post(event);
			
			if (!event.isCanceled())
			{
				armbot.grabObject(this.imprinterMatrix[craftingOutputSlot].copy());
				this.onPickUpFromSlot(null, 2, this.imprinterMatrix[craftingOutputSlot]);
				this.imprinterMatrix[craftingOutputSlot] = null;
				return true;
			}
		}
*/
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
		this.output = new ItemStack[1];

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
		return craftingSlots;
	}
}

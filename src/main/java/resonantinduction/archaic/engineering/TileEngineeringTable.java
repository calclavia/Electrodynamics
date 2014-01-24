package resonantinduction.archaic.engineering;

import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;

import resonantinduction.api.IArmbot;
import resonantinduction.api.IArmbotUseable;
import resonantinduction.archaic.imprint.ItemBlockImprint;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.prefab.ContainerDummy;
import resonantinduction.electrical.encoder.coding.args.ArgumentData;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.slot.ISlotPickResult;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.inventory.AutoCraftingManager;
import calclavia.lib.utility.inventory.AutoCraftingManager.IAutoCrafter;

import com.builtbroken.common.Pair;
import com.google.common.io.ByteArrayDataInput;

public class TileEngineeringTable extends TileAdvanced implements IPacketReceiver, IRotatable, ISidedInventory, IArmbotUseable, ISlotPickResult, IAutoCrafter
{
	public static final int CRAFTING_MATRIX_END = 9;
	public static final int CRAFTING_OUTPUT_END = CRAFTING_MATRIX_END + 1;
	public static final int PLAYER_OUTPUT_END = CRAFTING_OUTPUT_END + 40;
	public static final int CENTER_SLOT = 4;

	// Relative slot IDs
	public static final int CRAFTING_OUTPUT_SLOT = 0;
	private static final int IMPRINT_SLOT = 1;

	private AutoCraftingManager craftManager;

	/** 9 slots for crafting, 1 slot for a output. */
	public ItemStack[] craftingMatrix = new ItemStack[9];
	public static final int[] craftingSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };

	/** The output inventory containing slots. */
	public ItemStack[] inventory = new ItemStack[1];
	public static int[] inventorySlots;

	/** The ability for the engineering table to serach nearby inventories. */
	public boolean searchInventories = true;

	private InventoryPlayer invPlayer = null;
	private int[] playerSlots;

	/**
	 * Creates a "fake inventory" and hook the player up to the crafter to use the player's items.
	 */
	public void setPlayerInventory(InventoryPlayer invPlayer)
	{
		if (searchInventories)
		{
			if (invPlayer != null)
			{
				playerSlots = new int[invPlayer.getSizeInventory()];
				for (int i = 0; i < playerSlots.length; i++)
					playerSlots[i] = i + CRAFTING_OUTPUT_END;
			}
			else
			{
				playerSlots = null;
			}

			this.invPlayer = invPlayer;
		}
	}

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
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return ResonantInduction.PACKET_TILE.getPacket(this, nbt);
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			this.readFromNBT(PacketHandler.readNBTTagCompound(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 10 + (this.invPlayer != null ? this.invPlayer.getSizeInventory() : 0);
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
		else if (slot < CRAFTING_OUTPUT_END)
		{
			return this.inventory[slot - CRAFTING_MATRIX_END];
		}
		else if (slot < PLAYER_OUTPUT_END && invPlayer != null)
		{
			return this.invPlayer.getStackInSlot(slot - CRAFTING_OUTPUT_END);
		}
		else if (searchInventories)
		{
			int idDisplacement = PLAYER_OUTPUT_END;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = new Vector3(this).modifyPositionFromSide(dir).getTileEntity(worldObj);

				if (tile instanceof IInventory)
				{
					IInventory inventory = (IInventory) tile;
					int slotID = slot - idDisplacement;

					if (slotID < inventory.getSizeInventory())
						return inventory.getStackInSlot(slotID);

					idDisplacement += inventory.getSizeInventory();
				}
			}
		}

		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack)
	{
		if (slot < CRAFTING_MATRIX_END)
		{
			this.craftingMatrix[slot] = itemStack;
		}
		else if (slot < CRAFTING_OUTPUT_END)
		{
			this.inventory[slot - CRAFTING_MATRIX_END] = itemStack;
		}
		else if (slot < PLAYER_OUTPUT_END && this.invPlayer != null)
		{
			this.invPlayer.setInventorySlotContents(slot - CRAFTING_OUTPUT_END, itemStack);
			EntityPlayer player = this.invPlayer.player;

			if (player instanceof EntityPlayerMP)
			{
				((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
			}
		}
		else if (searchInventories)
		{
			int idDisplacement = PLAYER_OUTPUT_END;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = new Vector3(this).modifyPositionFromSide(dir).getTileEntity(worldObj);

				if (tile instanceof IInventory)
				{
					IInventory inventory = (IInventory) tile;
					int slotID = slot - idDisplacement;

					if (slotID < inventory.getSizeInventory())
						inventory.setInventorySlotContents(slotID, itemStack);

					idDisplacement += inventory.getSizeInventory();
				}
			}
		}

		onInventoryChanged();
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
		return this.getBlockType().getLocalizedName();
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
		InventoryCrafting inventoryCrafting = new InventoryCrafting(new ContainerDummy(this), 3, 3);

		for (int i = 0; i < this.craftingMatrix.length; i++)
		{
			inventoryCrafting.setInventorySlotContents(i, this.craftingMatrix[i]);
		}

		return inventoryCrafting;
	}

	/** Updates all the output slots. Call this to update the Engineering Table. */
	@Override
	public void onInventoryChanged()
	{
		if (worldObj != null)
		{
			if (!worldObj.isRemote)
			{
				this.inventory[CRAFTING_OUTPUT_SLOT] = null;

				/** Try to craft from crafting grid. If not possible, then craft from imprint. */
				boolean didCraft = false;

				/** Simulate an Inventory Crafting Instance */
				InventoryCrafting inventoryCrafting = this.getCraftingMatrix();

				ItemStack matrixOutput = CraftingManager.getInstance().findMatchingRecipe(inventoryCrafting, this.worldObj);

				if (matrixOutput != null && this.getCraftingManager().getIdealRecipe(matrixOutput) != null)
				{
					this.inventory[CRAFTING_OUTPUT_SLOT] = matrixOutput;
					didCraft = true;
				}

				/**
				 * If output does not exist, try using the filter.
				 */
				if (!didCraft)
				{
					ItemStack filterStack = craftingMatrix[CENTER_SLOT];

					if (filterStack != null && filterStack.getItem() instanceof ItemBlockImprint)
					{
						Set<ItemStack> filters = ItemBlockImprint.getFilters(filterStack);

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
										this.inventory[CRAFTING_OUTPUT_SLOT] = recipeOutput;
										didCraft = true;
										break;
									}
								}
							}
						}
					}
				}

				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}

	@Override
	public void onPickUpFromSlot(EntityPlayer entityPlayer, int s, ItemStack itemStack)
	{
		if (!worldObj.isRemote)
		{
			if (itemStack != null)
			{
				Pair<ItemStack, ItemStack[]> idealRecipeItem = this.getCraftingManager().getIdealRecipe(itemStack);

				if (idealRecipeItem != null)
				{
					this.getCraftingManager().consumeItems(idealRecipeItem.right().clone());
				}
				else
				{
					itemStack.stackSize = 0;
				}
			}
		}
	}

	/** Tries to let the Armbot craft an item. */
	@Override
	public boolean onUse(IArmbot armbot, List<ArgumentData> data)
	{
		this.onInventoryChanged();

		/*
		 * if (this.imprinterMatrix[craftingOutputSlot] != null)
		 * {
		 * AutoCraftEvent.PreCraft event = new AutoCraftEvent.PreCraft(this.worldObj, new
		 * Vector3(this), this, this.imprinterMatrix[craftingOutputSlot]);
		 * MinecraftForge.EVENT_BUS.post(event);
		 * if (!event.isCanceled())
		 * {
		 * armbot.grabObject(this.imprinterMatrix[craftingOutputSlot].copy());
		 * this.onPickUpFromSlot(null, 2, this.imprinterMatrix[craftingOutputSlot]);
		 * this.imprinterMatrix[craftingOutputSlot] = null;
		 * return true;
		 * }
		 * }
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

		NBTTagList nbtList = nbt.getTagList("Items");
		this.craftingMatrix = new ItemStack[9];
		this.inventory = new ItemStack[1];

		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound stackTag = (NBTTagCompound) nbtList.tagAt(i);
			byte id = stackTag.getByte("Slot");

			if (id >= 0 && id < this.getSizeInventory())
			{
				this.setInventorySlotContents(id, ItemStack.loadItemStackFromNBT(stackTag));
			}
		}

		this.searchInventories = nbt.getBoolean("searchInventories");
	}

	/** Writes a tile entity to NBT. */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		NBTTagList nbtList = new NBTTagList();

		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			if (this.getStackInSlot(i) != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) i);
				this.getStackInSlot(i).writeToNBT(var4);
				nbtList.appendTag(var4);
			}
		}

		nbt.setTag("Items", nbtList);
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
		int[] slots = craftingSlots;

		if (playerSlots != null)
		{
			slots = ArrayUtils.addAll(playerSlots, slots);
		}

		if (searchInventories)
		{
			int temporaryInvID = PLAYER_OUTPUT_END;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = new Vector3(this).modifyPositionFromSide(dir).getTileEntity(worldObj);

				if (tile instanceof IInventory)
				{
					IInventory inventory = (IInventory) tile;
					int[] nearbySlots = new int[inventory.getSizeInventory()];

					for (int i = 0; i < inventory.getSizeInventory(); i++)
					{
						nearbySlots[i] = temporaryInvID++;
					}

					slots = ArrayUtils.addAll(nearbySlots, slots);
				}
			}
		}

		return slots;
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(getBlockMetadata());
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal(), 3);
	}

}

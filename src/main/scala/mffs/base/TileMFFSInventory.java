package mffs.base;

import com.google.common.io.ByteArrayDataInput;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import mffs.ModularForceFieldSystem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.multiblock.TileMultiBlockPart;
import resonant.lib.network.PacketHandler;
import universalelectricity.core.transform.vector.Vector3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * All TileEntities that have an inventory should extend this.
 *
 * @author Calclavia
 */
public abstract class TileMFFSInventory extends TileMFFS implements IInventory
{
	/**
	 * The inventory of the TileEntity.
	 */
	protected ItemStack[] inventory = new ItemStack[this.getSizeInventory()];

	@Override
	public ArrayList getPacketData(int packetID)
	{
		ArrayList data = super.getPacketData(packetID);

		if (packetID == TilePacketType.DESCRIPTION.ordinal())
		{
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			data.add(nbt);
		}

		return data;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (this.worldObj.isRemote)
		{
			if (packetID == TilePacketType.DESCRIPTION.ordinal() || packetID == TilePacketType.INVENTORY.ordinal())
			{
				this.readFromNBT(PacketHandler.readNBTTagCompound(dataStream));
			}
		}
	}

	public void sendInventoryToClients()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.INVENTORY.ordinal(), nbt));
	}

	/**
	 * Inventory Methods
	 */
	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.inventory[i];
	}

	@Override
	public String getInvName()
	{
		if (this.getBlockType() != null)
		{
			return this.getBlockType().getLocalizedName();
		}

		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inventory[i] = itemstack;
		if ((itemstack != null) && (itemstack.stackSize > getInventoryStackLimit()))
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.inventory[i] != null)
		{
			if (this.inventory[i].stackSize <= j)
			{
				ItemStack itemstack = this.inventory[i];
				this.inventory[i] = null;
				return itemstack;
			}
			ItemStack itemstack1 = this.inventory[i].splitStack(j);
			if (this.inventory[i].stackSize == 0)
			{
				this.inventory[i] = null;
			}
			return itemstack1;
		}
		return null;
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this)
		{
			return false;
		}

		return true;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotID)
	{
		if (this.inventory[slotID] != null)
		{
			ItemStack itemstack = this.inventory[slotID];
			this.inventory[slotID] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
	{
		return true;
	}

	/**
	 * @return Returns if a specific slot is valid to input a specific itemStack.
	 */
	public boolean canIncreaseStack(int slotID, ItemStack itemStack)
	{
		if (this.getStackInSlot(slotID) == null)
		{
			return true;
		}
		else
		{
			if (this.getStackInSlot(slotID).stackSize + 1 <= 64)
			{
				return this.getStackInSlot(slotID).isItemEqual(itemStack);
			}
		}

		return false;
	}

	public void incrStackSize(int slot, ItemStack itemStack)
	{
		if (this.getStackInSlot(slot) == null)
		{
			this.setInventorySlotContents(slot, itemStack.copy());
		}
		else if (this.getStackInSlot(slot).isItemEqual(itemStack))
		{
			this.getStackInSlot(slot).stackSize++;
		}
	}

	public Set<ItemStack> getCards()
	{
		Set<ItemStack> cards = new HashSet<ItemStack>();
		cards.add(this.getStackInSlot(0));
		return cards;
	}

	/**
	 * Tries to place an itemStack in a specific position if it is an inventory.
	 *
	 * @return The ItemStack remained after place attempt
	 */
	public ItemStack tryPlaceInPosition(ItemStack itemStack, Vector3 position, ForgeDirection dir)
	{
		TileEntity tileEntity = position.getTileEntity(this.worldObj);
		ForgeDirection direction = dir.getOpposite();

		if (tileEntity != null && itemStack != null)
		{
			/** Try to put items into a chest. */
			if (tileEntity instanceof TileMultiBlockPart)
			{
				Vector3 mainBlockPosition = ((TileMultiBlockPart) tileEntity).getMainBlock();

				if (mainBlockPosition != null)
				{
					if (!(mainBlockPosition.getTileEntity(this.worldObj) instanceof TileMultiBlockPart))
					{
						return tryPlaceInPosition(itemStack, mainBlockPosition, direction);
					}
				}
			}
			else if (tileEntity instanceof TileEntityChest)
			{
				TileEntityChest[] chests = { (TileEntityChest) tileEntity, null };

				/** Try to find a double chest. */
				for (int i = 2; i < 6; i++)
				{
					ForgeDirection searchDirection = ForgeDirection.getOrientation(i);
					Vector3 searchPosition = position.clone();
					searchPosition.translate(searchDirection);

					if (searchPosition.getTileEntity(this.worldObj) != null)
					{
						if (searchPosition.getTileEntity(this.worldObj).getClass() == chests[0].getClass())
						{
							chests[1] = (TileEntityChest) searchPosition.getTileEntity(this.worldObj);
							break;
						}
					}
				}

				for (TileEntityChest chest : chests)
				{
					if (chest != null)
					{
						for (int i = 0; i < chest.getSizeInventory(); i++)
						{
							itemStack = this.addStackToInventory(i, chest, itemStack);
							if (itemStack == null)
							{
								return null;
							}
						}
					}
				}
			}
			else if (tileEntity instanceof ISidedInventory)
			{
				ISidedInventory inventory = (ISidedInventory) tileEntity;
				int[] slots = inventory.getAccessibleSlotsFromSide(direction.ordinal());
				for (int i = 0; i < slots.length; i++)
				{
					if (inventory.canInsertItem(slots[i], itemStack, direction.ordinal()))
					{
						itemStack = this.addStackToInventory(slots[i], inventory, itemStack);
					}
					if (itemStack == null)
					{
						return null;
					}
				}

			}
			else if (tileEntity instanceof IInventory)
			{
				IInventory inventory = (IInventory) tileEntity;

				for (int i = 0; i < inventory.getSizeInventory(); i++)
				{
					itemStack = this.addStackToInventory(i, inventory, itemStack);
					if (itemStack == null)
					{
						return null;
					}
				}
			}
		}

		if (itemStack.stackSize <= 0)
		{
			return null;
		}

		return itemStack;
	}

	public ItemStack addStackToInventory(int slotIndex, IInventory inventory, ItemStack itemStack)
	{
		if (inventory.getSizeInventory() > slotIndex)
		{
			ItemStack stackInInventory = inventory.getStackInSlot(slotIndex);

			if (stackInInventory == null)
			{
				inventory.setInventorySlotContents(slotIndex, itemStack);
				if (inventory.getStackInSlot(slotIndex) == null)
				{
					return itemStack;
				}
				return null;
			}
			else if (stackInInventory.isItemEqual(itemStack) && stackInInventory.isStackable())
			{
				stackInInventory = stackInInventory.copy();
				int stackLim = Math.min(inventory.getInventoryStackLimit(), itemStack.getMaxStackSize());
				int rejectedAmount = Math.max((stackInInventory.stackSize + itemStack.stackSize) - stackLim, 0);
				stackInInventory.stackSize = Math.min(Math.max((stackInInventory.stackSize + itemStack.stackSize - rejectedAmount), 0), inventory.getInventoryStackLimit());
				itemStack.stackSize = rejectedAmount;
				inventory.setInventorySlotContents(slotIndex, stackInInventory);
			}
		}

		if (itemStack.stackSize <= 0)
		{
			return null;
		}

		return itemStack;
	}

	public boolean mergeIntoInventory(ItemStack itemStack)
	{
		if (!this.worldObj.isRemote)
		{
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				if (itemStack != null)
				{
					itemStack = this.tryPlaceInPosition(itemStack, new Vector3(this).translate(direction), direction);
				}
			}

			if (itemStack != null)
			{
				this.worldObj.spawnEntityInWorld(new EntityItem(this.worldObj, this.xCoord + 0.5, this.yCoord + 1, this.zCoord + 0.5, itemStack));
			}
		}

		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);

		NBTTagList nbtTagList = nbttagcompound.getTagList("Items");
		this.inventory = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbtTagList.tagCount(); i++)
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtTagList.tagAt(i);

			byte byte0 = nbttagcompound1.getByte("Slot");
			if ((byte0 >= 0) && (byte0 < this.inventory.length))
			{
				this.inventory[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);

		NBTTagList nbtTagList = new NBTTagList();
		for (int i = 0; i < this.inventory.length; i++)
		{
			if (this.inventory[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.inventory[i].writeToNBT(nbttagcompound1);
				nbtTagList.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("Items", nbtTagList);
	}

	/**
	 * ComputerCraft
	 */
	@Override
	public String getType()
	{
		return this.getInvName();
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[] { "isActivate", "setActivate" };
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		switch (method)
		{
			case 0:
			{
				return new Object[] { this.isActive() };
			}
			case 1:
			{
				this.setActive((Boolean) arguments[0]);
				return null;
			}
		}

		throw new Exception("Invalid method.");

	}

	@Override
	public void attach(IComputerAccess computer)
	{

	}

	@Override
	public void detach(IComputerAccess computer)
	{

	}
}

package resonantinduction.old.transport.crate;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.network.ISimplePacketReceiver;
import resonantinduction.core.prefab.tile.TileEntityInv;
import resonantinduction.old.lib.interfaces.IExtendedStorage;
import resonantinduction.old.transport.ResonantInductionTransport;
import calclavia.lib.network.PacketHandler;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

/**
 * Basic single stack inventory
 * 
 * @author DarkGuardsman
 */
public class TileEntityCrate extends TileEntityInv implements ISimplePacketReceiver, IExtendedStorage
{
	/*
	 * TODO
	 * Fix issues with ItemStacks with NBT tags having issues
	 * Fix possible render issues with some items
	 * Yell at MachineMuse for her items rendering threw walls
	 * Add support to disable sides of crates when rendering items are unwanted
	 * Simplify item rendering to decrease graphic lag
	 * Add crafting manger to prevent crafting with full crates
	 * As well keep item stacks when upgrade crate threw crafting
	 * Add upgrade item for crate
	 * Add crate swapping in which an advanced can trade place with a basic while keeping inventory
	 * at the locaiton
	 */
	/** Collective total stack of all inv slots */
	private ItemStack sampleStack;

	/** delay from last click */
	public long prevClickTime = -1000;
	/** max meta size of the crate */
	public static final int maxSize = 2;

	@Override
	public InventoryCrate getInventory()
	{
		if (this.inventory == null)
		{
			inventory = new InventoryCrate(this);
		}
		return (InventoryCrate) this.inventory;
	}

	/** Gets the sample stack that represent the total inv */
	public ItemStack getSampleStack()
	{
		if (this.sampleStack == null)
		{
			this.buildSampleStack();
		}
		return this.sampleStack;
	}

	/**
	 * Turns the inventory array into a single stack of matching items. This assumes that all items
	 * in the crate are the same TODO eject minority items and only keep the majority that are the
	 * same to prevent duplication issues
	 * 
	 * @param force - force a rebuild of the inventory from the single stack created
	 */
	public void buildSampleStack()
	{
		ItemStack stack = null;

		boolean rebuildBase = false;

		/* Creates the sample stack that is used as a collective itemstack */
		for (int i = 0; i < this.getInventory().getContainedItems().length; i++)
		{
			ItemStack s = this.getInventory().getContainedItems()[i];
			if (s != null && s.itemID > 0 && s.stackSize > 0)
			{
				if (stack == null)
				{
					stack = s.copy();
				}
				else
				{
					stack.stackSize += this.getInventory().getContainedItems()[i].stackSize;
				}
				if (this.getInventory().getContainedItems()[i].stackSize > this.getInventory().getContainedItems()[i].getMaxStackSize())
				{
					rebuildBase = true;
				}
			}
		}
		if (stack == null || stack.itemID == 0 || stack.stackSize == 0)
		{
			this.sampleStack = null;
		}
		else
		{
			this.sampleStack = stack.copy();
		}
		/* if one stack is over sized this rebuilds the inv to redistribute the items in the slots */
		if ((rebuildBase || this.getInventory().getContainedItems().length > this.getSlotCount()) && this.sampleStack != null)
		{
			this.getInventory().buildInventory(this.sampleStack);
		}
	}

	/** Adds an item to the stack */
	public void addToStack(ItemStack stack, int amount)
	{
		if (stack != null)
		{
			this.addToStack(new ItemStack(stack.stackSize, amount, stack.getItemDamage()));
		}
	}

	/** Adds the stack to the sample stack */
	public void addToStack(ItemStack stack)
	{
		if (stack != null)
		{
			this.buildSampleStack();
			boolean flag = false;
			if (this.sampleStack == null)
			{
				this.sampleStack = stack;
				flag = true;
			}
			else if (this.sampleStack.isItemEqual(stack))
			{
				this.sampleStack.stackSize += stack.stackSize;
				flag = true;
			}
			if (flag)
			{
				this.getInventory().buildInventory(this.sampleStack);
				this.onInventoryChanged();
			}
		}
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();

		if (this.worldObj != null)
		{
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			{
				PacketHandler.instance().sendPacketToClients(this.getDescriptionPacket(), this.worldObj);
			}
		}
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return this.sampleStack == null || stack != null && stack.isItemEqual(sampleStack);
	}

	/** Gets the current slot count for the crate */
	public int getSlotCount()
	{
		if (this.worldObj == null)
		{
			return TileEntityCrate.getSlotCount(TileEntityCrate.maxSize);
		}
		return TileEntityCrate.getSlotCount(this.getBlockMetadata());
	}

	/** Gets the slot count for the crate meta */
	public static int getSlotCount(int metadata)
	{
		if (metadata >= 2)
		{
			return 256;
		}
		else if (metadata >= 1)
		{
			return 64;
		}
		return 32;
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public boolean simplePacket(String id, ByteArrayDataInput data, Player player)
	{
		if (this.worldObj.isRemote)
		{
			try
			{
				if (id.equalsIgnoreCase("InventoryItem"))
				{
					if (data.readBoolean())
					{
						this.sampleStack = ItemStack.loadItemStackFromNBT(PacketHandler.readNBTTagCompound(data));
						this.sampleStack.stackSize = data.readInt();
					}
					else
					{
						this.sampleStack = null;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return true;
			}
		}
		return false;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		this.buildSampleStack();
		ItemStack stack = this.getSampleStack();
		if (stack != null)
		{
			return PacketHandler.instance().getTilePacket(ResonantInductionTransport.CHANNEL, "InventoryItem", this, true, stack.writeToNBT(new NBTTagCompound()), stack.stackSize);
		}
		else
		{
			return PacketHandler.instance().getTilePacket(ResonantInductionTransport.CHANNEL, "InventoryItem", this, false);
		}
	}

	/** NBT Data */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		/* Load current two inv methods */
		ItemStack stack = null;
		int count = nbt.getInteger("Count");
		if (nbt.hasKey("itemID"))
		{
			stack = new ItemStack(nbt.getInteger("itemID"), count, nbt.getInteger("itemMeta"));
		}
		else
		{
			stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
			if (stack != null)
			{
				stack.stackSize = count;
			}
		}

		/* Only load sample stack if the read stack is valid */
		if (stack != null && stack.itemID != 0 && stack.stackSize > 0)
		{
			this.sampleStack = stack;
			this.getInventory().buildInventory(this.sampleStack);
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		/* Re-Build sample stack for saving */
		this.buildSampleStack();
		ItemStack stack = this.getSampleStack();
		/* Save sample stack */
		if (stack != null)
		{
			nbt.setInteger("Count", stack.stackSize);
			nbt.setCompoundTag("stack", stack.writeToNBT(new NBTTagCompound()));
		}

	}

	@Override
	public ItemStack addStackToStorage(ItemStack stack)
	{
		return BlockCrate.addStackToCrate(this, stack);
	}

}

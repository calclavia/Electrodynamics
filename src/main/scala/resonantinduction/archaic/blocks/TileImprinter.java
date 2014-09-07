package resonantinduction.archaic.blocks;

import codechicken.multipart.ControlKeyModifer;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import resonant.content.prefab.java.TileAdvanced;
import resonant.content.spatial.block.SpatialBlock;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonant.lib.render.RenderItemOverlayUtility;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import universalelectricity.core.transform.vector.Vector2;
import universalelectricity.core.transform.vector.Vector3;
import universalelectricity.core.transform.vector.VectorWorld;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TileImprinter extends TileAdvanced implements ISidedInventory, IPacketReceiver
{
	public ItemStack[] inventory = new ItemStack[10];

	public TileImprinter()
	{
		super(Material.circuits);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(this, nbt));
	}

	@Override
	public void read(ByteBuf data, EntityPlayer player, PacketType type)
	{
		try
		{
			this.readFromNBT(ByteBufUtils.readTag(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Inventory methods.
	 */
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public int getSizeInventory()
	{
		return this.inventory.length;
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
			inventory[slot] = itemStack;
		}
	}

	@Override
	public String getInventoryName()
	{
		return null;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
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
		return this.inventory[slot];
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
	public void openInventory()
	{
		this.onInventoryChanged();
	}

	@Override
	public void closeInventory()
	{
		this.onInventoryChanged();
	}

	/**
	 * Updates all the output slots. Call this to update the Imprinter.
	 */
	public void onInventoryChanged()
	{
		if (!this.worldObj.isRemote)
		{
			/** Makes the stamping recipe for filters */
			ItemStack fitlerStack = this.inventory[9];

			if (fitlerStack != null && fitlerStack.getItem() instanceof ItemImprint)
			{
				ItemStack outputStack = fitlerStack.copy();
				Set<ItemStack> filters = ItemImprint.getFilters(outputStack);
				Set<ItemStack> toAdd = new HashSet<ItemStack>();

				/** A hashset of to be imprinted items containing NO repeats. */
				Set<ItemStack> toBeImprinted = new HashSet<ItemStack>();

				check:
				for (int i = 0; i < 9; i++)
				{
					ItemStack stackInInventory = inventory[i];

					if (stackInInventory != null)
					{
						for (ItemStack check : toBeImprinted)
						{
							if (check.isItemEqual(stackInInventory))
							{
								continue check;
							}
						}

						toBeImprinted.add(stackInInventory);
					}
				}

				for (ItemStack stackInInventory : toBeImprinted)
				{
					Iterator<ItemStack> it = filters.iterator();

					boolean removed = false;

					while (it.hasNext())
					{
						ItemStack filteredStack = it.next();

						if (filteredStack.isItemEqual(stackInInventory))
						{
							it.remove();
							removed = true;
						}
					}

					if (!removed)
					{
						toAdd.add(stackInInventory);
					}
				}

				filters.addAll(toAdd);

				ItemImprint.setFilters(outputStack, filters);
				this.inventory[9] = outputStack;
			}
		}
	}

	// ///////////////////////////////////////
	// // Save And Data processing //////
	// ///////////////////////////////////////

	/**
	 * NBT Data
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		NBTTagList var2 = nbt.getTagList("Items", 0);
		this.inventory = new ItemStack[10];

		for (int i = 0; i < var2.tagCount(); ++i)
		{
			NBTTagCompound var4 = (NBTTagCompound) var2.getCompoundTagAt(i);
			byte var5 = var4.getByte("Slot");

			if (var5 >= 0 && var5 < this.getSizeInventory())
			{
				this.setInventorySlotContents(var5, ItemStack.loadItemStackFromNBT(var4));
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
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
	}

	// ///////////////////////////////////////
	// // Inventory Access side Methods //////
	// ///////////////////////////////////////

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
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : entityplayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return side == 1 ? new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 } : new int[10];
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
	public void renderDynamic(Vector3 position, float frame, int pass)
	{
		GL11.glPushMatrix();
		RenderItemOverlayUtility.renderTopOverlay(this, inventory, ForgeDirection.EAST, x(), y(), z());
		RenderItemOverlayUtility.renderItemOnSides(this, getStackInSlot(9), x(), y(), z());
		GL11.glPopMatrix();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconReg)
	{
		super.registerIcons(iconReg);
		SpatialBlock.icon().put("imprinter_side", iconReg.registerIcon(Reference.prefix() + "imprinter_side"));
		SpatialBlock.icon().put("imprinter_top", iconReg.registerIcon(Reference.prefix() + "imprinter_top"));
		SpatialBlock.icon().put("imprinter_bottom", iconReg.registerIcon(Reference.prefix() + "imprinter_bottom"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if (side == 1)
		{
			return SpatialBlock.icon().get("imprinter_top");

		}
		else if (side == 0)
		{
			return SpatialBlock.icon().get("imprinter_bottom");

		}

		return SpatialBlock.icon().get("imprinter_side");
	}

	public boolean use(EntityPlayer player, int hitSide, Vector3 hit)
	{
		ItemStack current = player.inventory.getCurrentItem();

		if (hitSide == 1)
		{
			if (!world().isRemote)
			{
				Vector2 hitVector = new Vector2(hit.x(), hit.z());
				double regionLength = 1d / 3d;

				/**
				 * Crafting Matrix
				 */
				matrix:
				for (int j = 0; j < 3; j++)
				{
					for (int k = 0; k < 3; k++)
					{
						Vector2 check = new Vector2(j, k).multiply(regionLength);

						if (check.distance(hitVector) < regionLength)
						{
							int slotID = j * 3 + k;
							boolean didInsert = false;
							ItemStack checkStack = inventory[slotID];

							if (current != null)
							{
								if (checkStack == null || checkStack.isItemEqual(current))
								{
									if (ControlKeyModifer.isControlDown(player))
									{
										if (checkStack == null)
										{
											inventory[slotID] = current;
										}
										else
										{
											inventory[slotID].stackSize += current.stackSize;
											current.stackSize = 0;
										}

										current = null;
									}
									else
									{
										if (checkStack == null)
										{
											inventory[slotID] = current.splitStack(1);
										}
										else
										{
											inventory[slotID].stackSize++;
											current.stackSize--;
										}
									}

									if (current == null || current.stackSize <= 0)
									{
										player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
									}

									didInsert = true;
								}
							}

							if (!didInsert && checkStack != null)
							{
								InventoryUtility.dropItemStack(world(), new Vector3(player), checkStack, 0);
								inventory[slotID] = null;
							}

							break matrix;
						}
					}
				}

				world().markBlockForUpdate(x(), y(), z());
			}

			return true;
		}
		else if (hitSide != 0)
		{

			ItemStack output = getStackInSlot(9);

			if (output != null)
			{
				InventoryUtility.dropItemStack(world(), new Vector3(player), output, 0);
				setInventorySlotContents(9, null);
			}
			else if (current != null && current.getItem() instanceof ItemImprint)
			{
				setInventorySlotContents(9, current);
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}
		}
		return false;
	}

	@Override
	public void onNeighborChanged(Block block)
	{
		VectorWorld vec = new VectorWorld(this);
		Block b = vec.add(ForgeDirection.getOrientation(1)).getBlock();

		if (Blocks.piston_head == b)
		{
			onInventoryChanged();
		}
	}
}

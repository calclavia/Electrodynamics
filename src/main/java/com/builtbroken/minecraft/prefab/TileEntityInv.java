package com.builtbroken.minecraft.prefab;

import java.util.ArrayList;
import java.util.List;

import resonatninduction.tilenetwork.prefab.NetworkTileEntities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.access.AccessGroup;
import calclavia.lib.access.AccessUser;
import calclavia.lib.access.GroupRegistry;
import calclavia.lib.access.ISpecialAccess;
import calclavia.lib.access.Nodes;
import calclavia.lib.prefab.tile.TileAdvanced;

import com.builtbroken.minecraft.interfaces.IExternalInv;
import com.builtbroken.minecraft.interfaces.IInvBox;
import com.builtbroken.minecraft.prefab.invgui.InvChest;

/**
 * Prefab for simple object who only need basic inv support and nothing more
 * 
 * @author Darkguardsman
 */
public class TileEntityInv extends TileAdvanced implements IExternalInv, ISidedInventory, ISpecialAccess
{
	protected IInvBox inventory;
	protected boolean lockInv;
	protected int invSlots = 1;
	private Vector3 thisPos;
	/** A list of user access data. */
	protected List<AccessGroup> groups = new ArrayList<AccessGroup>();

	@Override
	public void initiate()
	{
		thisPos = new Vector3(this);
	}

	public Vector3 getThisPos()
	{
		if (this.thisPos == null || this.thisPos.intX() != xCoord || this.thisPos.intY() != yCoord || this.thisPos.intZ() != zCoord)
		{
			this.thisPos = new Vector3(this);
		}
		return this.thisPos;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		NetworkTileEntities.invalidate(this);
	}

	@Override
	public IInvBox getInventory()
	{
		if (inventory == null)
		{
			inventory = new InvChest(this, this.invSlots);
		}
		return inventory;
	}

	/** Gets the container class that goes with this tileEntity when creating a gui */
	public Class<? extends Container> getContainer()
	{
		return null;
	}

	@Override
	public int getSizeInventory()
	{
		return this.getInventory().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.getInventory().getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return this.getInventory().decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return this.getInventory().getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.getInventory().setInventorySlotContents(i, itemstack);

	}

	@Override
	public String getInvName()
	{
		return this.getInventory().getInvName();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return this.getInventory().isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.getInventory().getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return this.getInventory().isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest()
	{
		this.getInventory().openChest();

	}

	@Override
	public void closeChest()
	{
		this.getInventory().closeChest();

	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return this.getInventory().isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return this.getInventory().getAccessibleSlotsFromSide(var1);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return this.getInventory().canInsertItem(i, itemstack, j);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return this.getInventory().canExtractItem(i, itemstack, j);
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return false;
	}

	@Override
	public boolean canRemove(ItemStack stack, int slot, ForgeDirection side)
	{
		if (slot >= this.getSizeInventory())
		{
			return false;
		}
		return true;
	}

	/*
	 * User access
	 */
	public boolean canOpen(String username)
	{
		return this.getUserAccess(username) != null && this.getUserAccess(username).hasNode(Nodes.INV_OPEN_NODE) || this.getOwnerGroup().getMembers().size() <= 0;
	}

	@Override
	public AccessUser getUserAccess(String username)
	{
		for (AccessGroup group : this.groups)
		{
			AccessUser user = group.getMember(username);
			if (user != null)
			{
				return user;
			}
		}
		return new AccessUser(username);
	}

	@Override
	public List<AccessUser> getUsers()
	{
		List<AccessUser> users = new ArrayList<AccessUser>();
		for (AccessGroup group : this.groups)
		{
			users.addAll(group.getMembers());
		}
		return users;
	}

	@Override
	public boolean setUserAccess(String player, AccessGroup g, boolean save)
	{
		return setUserAccess(new AccessUser(player).setTempary(save), g);
	}

	@Override
	public boolean setUserAccess(AccessUser user, AccessGroup group)
	{
		boolean bool = false;

		if (user != null && user.getName() != null)
		{
			bool = this.removeUserAccess(user.getName()) && group == null;
			if (group != null)
			{
				bool = group.addMemeber(user);
			}
			if (bool)
			{
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
		return bool;
	}

	public boolean removeUserAccess(String player)
	{
		boolean re = false;
		for (AccessGroup group : this.groups)
		{
			AccessUser user = group.getMember(player);
			if (user != null && group.removeMemeber(user))
			{
				re = true;
			}
		}
		if (re)
		{
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		return re;
	}

	@Override
	public AccessGroup getGroup(String name)
	{
		for (AccessGroup group : this.getGroups())
		{
			if (group.getName().equalsIgnoreCase(name))
			{
				return group;
			}
		}
		return null;
	}

	@Override
	public boolean addGroup(AccessGroup group)
	{
		if (!this.groups.contains(group))
		{
			for (AccessGroup g : this.groups)
			{
				if (group.getName().equalsIgnoreCase(g.getName()))
				{
					return false;
				}
			}
			return this.groups.add(group);
		}
		return false;
	}

	@Override
	public AccessGroup getOwnerGroup()
	{
		return this.getGroup("owner");
	}

	@Override
	public List<AccessGroup> getGroups()
	{
		if (this.groups == null || this.groups.isEmpty())
		{
			GroupRegistry.loadNewGroupSet(this);
		}
		return this.groups;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.getInventory().loadInv(nbt);
		NBTTagList userList = nbt.getTagList("groups");
		if (userList != null && userList.tagCount() > 0)
		{
			this.groups.clear();
			for (int i = 0; i < userList.tagCount(); i++)
			{
				AccessGroup group = new AccessGroup("");
				group.load((NBTTagCompound) userList.tagAt(i));
				this.groups.add(group);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		this.getInventory().saveInv(nbt);

		NBTTagList usersTag = new NBTTagList();
		for (AccessGroup group : this.getGroups())
		{
			usersTag.appendTag(group.save(new NBTTagCompound()));
		}
		nbt.setTag("groups", usersTag);
	}

}

package mffs.tileentity;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.modules.IInterdictionMatrixModule;
import mffs.api.modules.IModule;
import mffs.api.security.IBiometricIdentifier;
import mffs.api.security.IInterdictionMatrix;
import mffs.api.security.Permission;
import mffs.base.TileEntityModuleAcceptor;
import mffs.card.ItemCard;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityInterdictionMatrix extends TileEntityModuleAcceptor implements IInterdictionMatrix
{
	/**
	 * True if the current confiscation mode is for "banning selected items".
	 */
	private boolean isBanMode = true;

	public TileEntityInterdictionMatrix()
	{
		this.fortronTank.setCapacity(20 * LiquidContainerRegistry.BUCKET_VOLUME);
		this.startModuleIndex = 2;
		this.endModuleIndex = 9;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.isActive() || (this.getStackInSlot(0) != null && this.getStackInSlot(0).itemID == ModularForceFieldSystem.itemCardInfinite.itemID))
			{
				if (this.ticks % 10 == 0)
				{
					if (this.requestFortron(this.getFortronCost() * 10, false) > 0)
					{
						this.requestFortron(this.getFortronCost() * 10, true);
						this.scan();
					}
				}
			}
		}
	}

	@Override
	public float getAmplifier()
	{
		return Math.max(Math.min((this.getActionRange() / 20), 10), 1);
	}

	/**
	 * Scans the surroundings.
	 */
	public void scan()
	{
		try
		{
			IBiometricIdentifier biometricIdentifier = this.getBiometricIdentifier();

			AxisAlignedBB emptyBounds = AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord, this.yCoord, this.zCoord);

			List<EntityLiving> warningList = this.worldObj.getEntitiesWithinAABB(EntityLiving.class, emptyBounds.expand(getWarningRange(), getWarningRange(), getWarningRange()));
			List<EntityLiving> actionList = this.worldObj.getEntitiesWithinAABB(EntityLiving.class, emptyBounds.expand(getActionRange(), getActionRange(), getActionRange()));

			for (EntityLiving entityLiving : warningList)
			{
				if (entityLiving instanceof EntityPlayer && !actionList.contains(entityLiving))
				{
					EntityPlayer player = (EntityPlayer) entityLiving;
					double distance = Vector3.distance(new Vector3(this), new Vector3(entityLiving));

					if (distance <= this.getWarningRange())
					{
						boolean isGranted = false;

						if (biometricIdentifier != null && biometricIdentifier.isAccessGranted(player.username, Permission.BYPASS_INTERDICTION_MATRIX))
						{
							isGranted = true;
						}

						if (!isGranted && this.worldObj.rand.nextInt(3) == 0)
						{
							player.addChatMessage("[" + this.getInvName() + "] Warning! You are in scanning range!");
							player.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, 1);
						}

					}
				}
			}

			if (this.worldObj.rand.nextInt(3) == 0)
			{
				for (EntityLiving entityLiving : actionList)
				{
					double distance = Vector3.distance(new Vector3(this), new Vector3(entityLiving));

					if (distance <= this.getActionRange())
					{
						this.applyAction(entityLiving);
					}
				}
			}

		}
		catch (Exception e)
		{
			ModularForceFieldSystem.LOGGER.severe("Defense Station has an error!");
			e.printStackTrace();
		}
	}

	/**
	 * Applies an action.
	 * 
	 * @param entityLiving
	 */
	public void applyAction(EntityLiving entityLiving)
	{
		/**
		 * Check for security permission to see if this player should be ignored.
		 */
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;

			IBiometricIdentifier biometricIdentifier = this.getBiometricIdentifier();

			if (biometricIdentifier != null && biometricIdentifier.isAccessGranted(player.username, Permission.BYPASS_INTERDICTION_MATRIX))
			{
				return;
			}

			if (!Settings.INTERACT_CREATIVE && player.capabilities.isCreativeMode)
			{
				return;
			}
		}

		for (ItemStack itemStack : this.getModuleStacks())
		{
			if (itemStack.getItem() instanceof IInterdictionMatrixModule)
			{
				IInterdictionMatrixModule module = (IInterdictionMatrixModule) itemStack.getItem();
				if (module.onDefend(this, entityLiving) || entityLiving.isDead)
				{

					break;
				}
			}
		}
	}

	@Override
	public List getPacketUpdate()
	{
		List objects = new LinkedList();
		objects.addAll(super.getPacketUpdate());
		objects.add(this.isBanMode);
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.DESCRIPTION.ordinal())
		{
			this.isBanMode = dataStream.readBoolean();
		}
		else if (packetID == TilePacketType.TOGGLE_MODE.ordinal())
		{
			this.isBanMode = !this.isBanMode;
		}
	}

	public boolean isBanMode()
	{
		return this.isBanMode;
	}

	@Override
	public int getActionRange()
	{
		return this.getModuleCount(ModularForceFieldSystem.itemModuleScale);
	}

	@Override
	public int getWarningRange()
	{
		return this.getModuleCount(ModularForceFieldSystem.itemModuleWarn) + this.getActionRange() + 3;
	}

	@Override
	public boolean mergeIntoInventory(ItemStack itemStack)
	{
		for (int dir = 0; dir < 5; dir++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(dir);
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), direction);

			if (tileEntity instanceof IInventory)
			{
				IInventory inventory = (IInventory) tileEntity;

				for (int i = 0; i < inventory.getSizeInventory(); i++)
				{
					ItemStack checkStack = inventory.getStackInSlot(i);

					if (checkStack == null)
					{
						inventory.setInventorySlotContents(i, itemStack);
						return true;
					}
					else if (checkStack.isItemEqual(itemStack))
					{
						int freeSpace = checkStack.getMaxStackSize() - checkStack.stackSize;

						checkStack.stackSize += Math.min(itemStack.stackSize, freeSpace);
						itemStack.stackSize -= freeSpace;

						if (itemStack.stackSize <= 0)
						{
							itemStack = null;
							return true;
						}
					}
				}
			}
		}

		this.worldObj.spawnEntityInWorld(new EntityItem(this.worldObj, this.xCoord + 0.5, this.yCoord + 1, this.zCoord + 0.5, itemStack));

		return false;
	}

	@Override
	public int getSizeInventory()
	{
		return 2 + 8 + 9;
	}

	@Override
	public Set<ItemStack> getFilteredItems()
	{
		Set<ItemStack> stacks = new HashSet<ItemStack>();

		for (int i = this.endModuleIndex; i < this.getSizeInventory() - 1; i++)
		{
			if (this.getStackInSlot(i) != null)
			{
				stacks.add(this.getStackInSlot(i));
			}
		}
		return stacks;
	}

	@Override
	public boolean getFilterMode()
	{
		return this.isBanMode;
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemStack)
	{
		if (slotID == 0 || slotID == 1)
		{
			return itemStack.getItem() instanceof ItemCard;
		}

		if (slotID > this.endModuleIndex)
		{
			return true;
		}

		return itemStack.getItem() instanceof IModule;
	}

	@Override
	public Set<ItemStack> getCards()
	{
		Set<ItemStack> cards = new HashSet<ItemStack>();
		cards.add(super.getCard());
		cards.add(this.getStackInSlot(1));
		return cards;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.isBanMode = nbt.getBoolean("isBanMode");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isBanMode", this.isBanMode);
	}
}
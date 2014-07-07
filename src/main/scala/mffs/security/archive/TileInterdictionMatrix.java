package mffs.security.archive;

import mffs.security.access.MFFSPermissions;
import resonant.api.mffs.modules.IInterdictionMatrixModule;
import resonant.api.mffs.modules.IModule;
import resonant.api.mffs.security.IBiometricIdentifier;
import resonant.api.mffs.security.IInterdictionMatrix;
import com.google.common.io.ByteArrayDataInput;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.base.TileModuleAcceptor;
import mffs.item.card.ItemCard;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import resonant.lib.utility.LanguageUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileInterdictionMatrix extends TileModuleAcceptor implements IInterdictionMatrix
{
	/**
	 * True if the current confiscation mode is for "banning selected items".
	 */
	private boolean isBanMode = true;

	public TileInterdictionMatrix()
	{
		this.capacityBase = 30;
		this.startModuleIndex = 2;
		this.endModuleIndex = 9;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (isActive() || (this.getStackInSlot(0) != null && this.getStackInSlot(0).itemID == ModularForceFieldSystem.itemCardInfinite.itemID))
			{
				if (this.ticks % 10 == 0)
				{
					if (this.requestFortron(this.getFortronCost() * 10, false) > 0)
					{
						this.requestFortron(this.getFortronCost() * 10, true);
						this.scan();
					}
					else
					{
						setActive(false);
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

			AxisAlignedBB emptyBounds = AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);

			List<EntityLivingBase> warningList = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, emptyBounds.expand(getWarningRange(), getWarningRange(), getWarningRange()));
			List<EntityLivingBase> actionList = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, emptyBounds.expand(getActionRange(), getActionRange(), getActionRange()));

			for (EntityLivingBase entityLiving : warningList)
			{
				if (entityLiving instanceof EntityPlayer && !actionList.contains(entityLiving))
				{
					EntityPlayer player = (EntityPlayer) entityLiving;

					boolean isGranted = false;

					if (biometricIdentifier != null && biometricIdentifier.hasPermission(player.username, MFFSPermissions.BYPASS_INTERDICTION_MATRIX))
					{
						isGranted = true;
					}

					if (!isGranted && this.worldObj.rand.nextInt(3) == 0 && getModuleCount(ModularForceFieldSystem.itemModuleSilence) <= 0)
					{
						player.addChatMessage("[" + this.getInvName() + "] " + LanguageUtility.getLocal("message.interdictionMatrix.warn"));
					}

				}
			}

			if (this.worldObj.rand.nextInt(3) == 0)
			{
				for (EntityLivingBase entityLiving : actionList)
				{
					this.applyAction(entityLiving);
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
	public void applyAction(EntityLivingBase entityLiving)
	{
		/**
		 * Check for security permission to see if this player should be ignored.
		 */
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;

			IBiometricIdentifier biometricIdentifier = this.getBiometricIdentifier();

			if (biometricIdentifier != null && biometricIdentifier.hasPermission(player.username, MFFSPermissions.BYPASS_INTERDICTION_MATRIX))
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
	public ArrayList getPacketData(int packetID)
	{
		ArrayList objects = super.getPacketData(packetID);
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
		return Math.min(this.getModuleCount(ModularForceFieldSystem.itemModuleScale), Settings.INTERDICTION_MAX_RANGE);
	}

	@Override
	public int getWarningRange()
	{
		return Math.min(this.getModuleCount(ModularForceFieldSystem.itemModuleWarn) + this.getActionRange(), Settings.INTERDICTION_MAX_RANGE) + 3;
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
	public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
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
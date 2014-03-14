package mffs.tile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.base.TileFrequency;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.item.ItemStack;
import calclavia.api.mffs.card.ICardIdentification;
import calclavia.api.mffs.security.IBiometricIdentifier;
import calclavia.api.mffs.security.Permission;

import com.google.common.io.ByteArrayDataInput;

public class TileBiometricIdentifier extends TileFrequency implements IBiometricIdentifier
{
	public static final int SLOT_COPY = 12;

	@Override
	public boolean isAccessGranted(String username, Permission permission)
	{
		if (!isActive())
		{
			return true;
		}

		/**
		 * Check if the player is an operator or not.
		 */
		if (ModularForceFieldSystem.proxy.isOp(username) && Settings.OP_OVERRIDE)
		{
			return true;
		}

		/**
		 * Check if ID card is in this inventory.
		 */
		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			ItemStack itemStack = this.getStackInSlot(i);

			if (itemStack != null && itemStack.getItem() instanceof ICardIdentification)
			{
				if (username.equalsIgnoreCase(((ICardIdentification) itemStack.getItem()).getUsername(itemStack)))
				{
					if (((ICardIdentification) itemStack.getItem()).hasPermission(itemStack, permission))
					{
						return true;
					}
				}
			}
		}

		return username.equalsIgnoreCase(this.getOwner());
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.TOGGLE_MODE.ordinal())
		{
			if (this.getManipulatingCard() != null)
			{
				ICardIdentification idCard = (ICardIdentification) this.getManipulatingCard().getItem();

				int id = dataStream.readInt();
				Permission permission = Permission.getPermission(id);

				if (permission != null)
				{
					if (!idCard.hasPermission(this.getManipulatingCard(), permission))
					{
						idCard.addPermission(this.getManipulatingCard(), permission);
					}
					else
					{
						idCard.removePermission(this.getManipulatingCard(), permission);
					}
				}
				else
				{
					ModularForceFieldSystem.LOGGER.severe("Error handling security station permission packet: " + id + " - " + permission);
				}
			}
		}
		else if (packetID == TilePacketType.STRING.ordinal())
		{
			if (this.getManipulatingCard() != null)
			{
				ICardIdentification idCard = (ICardIdentification) this.getManipulatingCard().getItem();
				idCard.setUsername(this.getManipulatingCard(), dataStream.readUTF());
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
	{
		if (slotID == 0)
		{
			return itemStack.getItem() instanceof ItemCardFrequency;
		}
		else
		{
			return itemStack.getItem() instanceof ICardIdentification;
		}
	}

	@Override
	public String getOwner()
	{
		ItemStack itemStack = this.getStackInSlot(2);

		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof ICardIdentification)
			{
				return ((ICardIdentification) itemStack.getItem()).getUsername(itemStack);
			}
		}

		return null;
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();

		// Try to copy ID card data.
		if (this.getManipulatingCard() != null && this.getStackInSlot(SLOT_COPY) != null && this.getStackInSlot(SLOT_COPY).getItem() instanceof ICardIdentification)
		{
			ICardIdentification masterCard = ((ICardIdentification) this.getManipulatingCard().getItem());
			ICardIdentification copyCard = ((ICardIdentification) this.getStackInSlot(SLOT_COPY).getItem());

			for (Permission permission : Permission.getPermissions())
			{
				if (masterCard.hasPermission(this.getManipulatingCard(), permission))
				{
					copyCard.addPermission(this.getStackInSlot(SLOT_COPY), permission);
				}
				else
				{
					copyCard.removePermission(this.getStackInSlot(SLOT_COPY), permission);
				}
			}
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 13;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public ItemStack getManipulatingCard()
	{
		if (this.getStackInSlot(1) != null)
		{
			if (this.getStackInSlot(1).getItem() instanceof ICardIdentification)
			{
				return this.getStackInSlot(1);

			}
		}

		return null;
	}

	@Override
	public void setActive(boolean flag)
	{
		if (this.getOwner() != null || !flag)
		{
			super.setActive(flag);
		}
	}

	@Override
	public Set<IBiometricIdentifier> getBiometricIdentifiers()
	{
		Set<IBiometricIdentifier> set = new HashSet<IBiometricIdentifier>();
		set.add(this);
		return set;
	}
}
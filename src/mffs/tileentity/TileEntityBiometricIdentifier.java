package mffs.tileentity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mffs.ModularForceFieldSystem;
import mffs.api.card.ICardIdentification;
import mffs.api.security.IBiometricIdentifier;
import mffs.api.security.Permission;
import mffs.base.TileEntityFrequency;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.item.ItemStack;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityBiometricIdentifier extends TileEntityFrequency implements IBiometricIdentifier
{
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
		if (ModularForceFieldSystem.proxy.isOp(username))
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

		if (packetID == 3)
		{
			if (this.getManipulatingCard() != null)
			{
				ICardIdentification idCard = (ICardIdentification) this.getManipulatingCard().getItem();

				Permission permission = Permission.getPermission(dataStream.readInt());

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
				{
					ModularForceFieldSystem.LOGGER.severe("Error handling security station permission packet!");
				}
			}
		}
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemStack)
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
	public int getSizeInventory()
	{
		return 12;
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
	public boolean canUpdate()
	{
		return false;
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
	public List<IBiometricIdentifier> getBiometricIdentifiers()
	{
		List<IBiometricIdentifier> list = new ArrayList<IBiometricIdentifier>();
		list.add(this);
		return list;
	}
}
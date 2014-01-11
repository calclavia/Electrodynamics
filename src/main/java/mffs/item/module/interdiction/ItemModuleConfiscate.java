package mffs.item.module.interdiction;

import java.util.Set;

import mffs.api.security.IBiometricIdentifier;
import mffs.api.security.IInterdictionMatrix;
import mffs.api.security.Permission;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import calclavia.lib.utility.LanguageUtility;

public class ItemModuleConfiscate extends ItemModuleInterdictionMatrix
{
	public ItemModuleConfiscate(int i)
	{
		super(i, "moduleConfiscate");
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLivingBase entityLiving)
	{
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;

			IBiometricIdentifier biometricIdentifier = interdictionMatrix.getBiometricIdentifier();

			if (biometricIdentifier != null && biometricIdentifier.isAccessGranted(player.username, Permission.DEFENSE_STATION_CONFISCATION))
			{
				return false;
			}
		}

		Set<ItemStack> controlledStacks = interdictionMatrix.getFilteredItems();

		int confiscationCount = 0;
		IInventory inventory = null;

		if (entityLiving instanceof EntityPlayer)
		{
			IBiometricIdentifier biometricIdentifier = interdictionMatrix.getBiometricIdentifier();

			if (biometricIdentifier != null && biometricIdentifier.isAccessGranted(((EntityPlayer) entityLiving).username, Permission.BYPASS_INTERDICTION_MATRIX))
			{
				return false;
			}

			EntityPlayer player = (EntityPlayer) entityLiving;
			inventory = player.inventory;
		}
		else if (entityLiving instanceof IInventory)
		{
			inventory = (IInventory) entityLiving;
		}

		if (inventory != null)
		{
			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				// The ItemStack currently being checked.
				ItemStack checkStack = inventory.getStackInSlot(i);

				if (checkStack != null)
				{
					boolean stacksMatch = false;

					for (ItemStack itemStack : controlledStacks)
					{
						if (itemStack != null)
						{
							if (itemStack.isItemEqual(checkStack))
							{
								stacksMatch = true;
								break;
							}
						}
					}

					if ((interdictionMatrix.getFilterMode() && stacksMatch) || (!interdictionMatrix.getFilterMode() && !stacksMatch))
					{
						interdictionMatrix.mergeIntoInventory(inventory.getStackInSlot(i));
						inventory.setInventorySlotContents(i, null);
						confiscationCount++;
					}
				}
			}

			if (confiscationCount > 0 && entityLiving instanceof EntityPlayer)
			{
				((EntityPlayer) entityLiving).addChatMessage("[" + interdictionMatrix.getInvName() + "] " + LanguageUtility.getLocal("message.moduleConfiscate.confiscate").replaceAll("%p", "" + confiscationCount));
			}

			interdictionMatrix.requestFortron(confiscationCount, true);
		}

		return false;
	}
}
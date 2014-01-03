package mffs.item.module.interdiction;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.security.IInterdictionMatrix;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import calclavia.lib.prefab.TranslationHelper;

public class ItemModuleAntiPersonnel extends ItemModuleInterdictionMatrix
{
	public ItemModuleAntiPersonnel(int i)
	{
		super(i, "moduleAntiPersonnel");
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLivingBase entityLiving)
	{
		boolean hasPermission = false;

		if (!hasPermission && entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;

			if (!player.capabilities.isCreativeMode && !player.isEntityInvulnerable())
			{
				for (int i = 0; i < player.inventory.getSizeInventory(); i++)
				{
					if (player.inventory.getStackInSlot(i) != null)
					{
						interdictionMatrix.mergeIntoInventory(player.inventory.getStackInSlot(i));
						player.inventory.setInventorySlotContents(i, null);
					}
				}
				player.setHealth(1);
				player.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, 100);
				interdictionMatrix.requestFortron(Settings.INTERDICTION_MURDER_ENERGY, false);

				player.addChatMessage("[" + interdictionMatrix.getInvName() + "] "+TranslationHelper.getLocal("message.moduleAntiPersonnel.death"));
			}
		}

		return false;
	}
}
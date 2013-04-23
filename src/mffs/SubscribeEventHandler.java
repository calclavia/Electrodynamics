package mffs;

import mffs.api.security.IInterdictionMatrix;
import mffs.api.security.Permission;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.liquids.LiquidDictionary;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SubscribeEventHandler
{
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event)
	{
		if (event.map == Minecraft.getMinecraft().renderEngine.textureMapItems)
		{
			LiquidDictionary.getCanonicalLiquid("Fortron").setRenderingIcon(ModularForceFieldSystem.itemFortron.getIconFromDamage(0)).setTextureSheet("/gui/items.png");
		}
	}

	@ForgeSubscribe
	public void playerInteractEvent(PlayerInteractEvent evt)
	{
		if (evt.action == Action.RIGHT_CLICK_BLOCK || evt.action == Action.LEFT_CLICK_BLOCK)
		{
			/**
			 * Disable block breaking of force fields.
			 */
			if (evt.action == Action.LEFT_CLICK_BLOCK && evt.entityPlayer.worldObj.getBlockId(evt.x, evt.y, evt.z) == ModularForceFieldSystem.blockForceField.blockID)
			{
				evt.setCanceled(true);
				return;
			}

			if (evt.entityPlayer.capabilities.isCreativeMode)
			{
				return;
			}

			/**
			 * Check if Interdiction Matrix blocked a specific action.
			 */
			IInterdictionMatrix interdictionMatrix = MFFSHelper.getNearestInterdictionMatrix(evt.entityPlayer.worldObj, new Vector3(evt.entityPlayer));

			if (interdictionMatrix != null)
			{
				boolean hasPermission = true;

				if (evt.action == Action.RIGHT_CLICK_BLOCK && evt.entityPlayer.worldObj.getBlockTileEntity(evt.x, evt.y, evt.z) != null)
				{
					if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleBlockAccess) > 0)
					{
						hasPermission = false;

						if (MFFSHelper.isPermittedByInterdictionMatrix(interdictionMatrix, evt.entityPlayer.username, Permission.BLOCK_ACCESS))
						{
							hasPermission = true;
						}
					}
				}

				if (hasPermission)
				{

					if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleBlockAlter) > 0)
					{
						hasPermission = false;

						if (MFFSHelper.isPermittedByInterdictionMatrix(interdictionMatrix, evt.entityPlayer.username, Permission.BLOCK_ALTER))
						{
							hasPermission = true;
						}
					}
				}

				if (!hasPermission)
				{
					evt.entityPlayer.sendChatToPlayer("[" + ModularForceFieldSystem.blockInterdictionMatrix.getLocalizedName() + "] You have no permission to do that!");
					evt.setCanceled(true);
				}
			}
		}
	}

	@ForgeSubscribe
	public void livingSpawnEvent(LivingSpawnEvent evt)
	{
		IInterdictionMatrix interdictionMatrix = MFFSHelper.getNearestInterdictionMatrix(evt.world, new Vector3(evt.entityLiving));

		if (interdictionMatrix != null)
		{
			if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleAntiSpawn) > 0)
			{
				evt.setResult(Result.DENY);
			}
		}
	}
}

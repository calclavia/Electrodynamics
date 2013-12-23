package mffs;

import java.util.HashMap;

import mffs.api.EventStabilize;
import mffs.api.fortron.FrequencyGrid;
import mffs.api.fortron.IFortronFrequency;
import mffs.api.security.IInterdictionMatrix;
import mffs.api.security.Permission;
import mffs.fortron.FortronHelper;
import mffs.tileentity.TileEntityForceFieldProjector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.item.ItemSkull;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.Icon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SubscribeEventHandler
{
	public static final HashMap<String, Icon> fluidIconMap = new HashMap<String, Icon>();

	public void registerIcon(String name, TextureStitchEvent.Pre event)
	{
		fluidIconMap.put(name, event.map.registerIcon(name));
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void preTextureHook(TextureStitchEvent.Pre event)
	{
		if (event.map.textureType == 0)
		{
			registerIcon(ModularForceFieldSystem.PREFIX + "fortron", event);
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event)
	{
		FortronHelper.FLUID_FORTRON.setIcons(fluidIconMap.get(ModularForceFieldSystem.PREFIX + "fortron"));
	}

	/**
	 * Special stabilization cases.
	 * 
	 * @param evt
	 */
	@ForgeSubscribe
	public void eventStabilize(EventStabilize evt)
	{
		if (evt.itemStack.getItem() instanceof ItemSkull)
		{
			evt.world.setBlock(evt.x, evt.y, evt.z, Block.skull.blockID, evt.itemStack.getItemDamage(), 2);

			TileEntity tileentity = evt.world.getBlockTileEntity(evt.x, evt.y, evt.z);

			if (tileentity instanceof TileEntitySkull)
			{
				String s = "";

				if (evt.itemStack.hasTagCompound() && evt.itemStack.getTagCompound().hasKey("SkullOwner"))
				{
					s = evt.itemStack.getTagCompound().getString("SkullOwner");
				}

				((TileEntitySkull) tileentity).setSkullType(evt.itemStack.getItemDamage(), s);
				((BlockSkull) Block.skull).makeWither(evt.world, evt.x, evt.y, evt.z, (TileEntitySkull) tileentity);
			}

			--evt.itemStack.stackSize;
			evt.setCanceled(true);
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

			Vector3 position = new Vector3(evt.x, evt.y, evt.z);

			/**
			 * Check if Interdiction Matrix blocked a specific action.
			 */
			IInterdictionMatrix interdictionMatrix = MFFSHelper.getNearestInterdictionMatrix(evt.entityPlayer.worldObj, position);

			if (interdictionMatrix != null)
			{
				int blockID = position.getBlockID(evt.entityPlayer.worldObj);

				if (ModularForceFieldSystem.blockBiometricIdentifier.blockID == blockID && MFFSHelper.isPermittedByInterdictionMatrix(interdictionMatrix, evt.entityPlayer.username, Permission.SECURITY_CENTER_CONFIGURE))
				{
					return;
				}

				boolean hasPermission = MFFSHelper.hasPermission(evt.entityPlayer.worldObj, new Vector3(evt.x, evt.y, evt.z), interdictionMatrix, evt.action, evt.entityPlayer);

				if (!hasPermission)
				{
					evt.entityPlayer.addChatMessage("[" + ModularForceFieldSystem.blockInterdictionMatrix.getLocalizedName() + "] You have no permission to do that!");
					evt.setCanceled(true);
				}
			}
		}
	}

	/**
	 * When a block breaks, mark force field projectors for an update.
	 * @param evt
	 */
	@ForgeSubscribe
	public void blockBreakEvent(BreakEvent evt)
	{
		for (IFortronFrequency fortronFrequency : FrequencyGrid.instance().getFortronTiles(evt.world))
		{
			if (fortronFrequency instanceof TileEntityForceFieldProjector)
			{
				TileEntityForceFieldProjector projector = (TileEntityForceFieldProjector) fortronFrequency;

				if (projector.getCalculatedField() != null)
				{
					if (projector.getCalculatedField().contains(new Vector3(evt.x, evt.y, evt.z)))
					{
						projector.markFieldUpdate = true;
					}
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

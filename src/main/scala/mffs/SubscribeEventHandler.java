package mffs;

import resonant.api.mffs.EventForceManipulate.EventPreForceManipulate;
import resonant.api.mffs.EventStabilize;
import resonant.api.mffs.fortron.FrequencyGrid;
import resonant.api.mffs.fortron.IFortronFrequency;
import resonant.api.mffs.security.IInterdictionMatrix;
import resonant.api.mffs.security.Permission;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.base.TileFortron;
import mffs.tile.TileForceFieldProjector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.entity.player.EntityPlayer;
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
import resonant.lib.event.ChunkModifiedEvent.ChunkSetBlockEvent;
import universalelectricity.api.vector.Vector3;

import java.util.HashMap;

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

	@ForgeSubscribe
	public void eventPreForceManipulate(EventPreForceManipulate evt)
	{
		TileEntity tileEntity = evt.world.getBlockTileEntity(evt.beforeX, evt.beforeY, evt.beforeZ);

		if (tileEntity instanceof TileFortron)
		{
			((TileFortron) tileEntity).markSendFortron = false;
		}
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
	 *
	 * @param evt
	 */
	@ForgeSubscribe
	public void chunkModifyEvent(ChunkSetBlockEvent evt)
	{
		if (!evt.world.isRemote && evt.blockID == 0)
		{
			for (IFortronFrequency fortronFrequency : FrequencyGrid.instance().getFortronTiles(evt.world))
			{
				if (fortronFrequency instanceof TileForceFieldProjector)
				{
					TileForceFieldProjector projector = (TileForceFieldProjector) fortronFrequency;

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
	}

	@ForgeSubscribe
	public void livingSpawnEvent(LivingSpawnEvent evt)
	{
		IInterdictionMatrix interdictionMatrix = MFFSHelper.getNearestInterdictionMatrix(evt.world, new Vector3(evt.entityLiving));

		if (interdictionMatrix != null && !(evt.entity instanceof EntityPlayer))
		{
			if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleAntiSpawn) > 0)
			{
				evt.setResult(Result.DENY);
			}
		}
	}
}

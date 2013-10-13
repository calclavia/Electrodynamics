package mffs;

import mffs.api.EventStabilize;
import mffs.api.security.IInterdictionMatrix;
import mffs.api.security.Permission;
import mffs.item.module.projector.ItemModuleRepulsion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.item.ItemSkull;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent.CanUpdate;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.FluidRegistry;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SubscribeEventHandler
{
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event)
	{
		FluidRegistry.getFluid("fortron").setIcons(ModularForceFieldSystem.itemFortron.getIconFromDamage(0));
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

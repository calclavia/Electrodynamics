package mffs.item.module.projector;

import calclavia.api.mffs.IProjector;
import calclavia.api.mffs.security.IBiometricIdentifier;
import calclavia.api.mffs.security.Permission;
import mffs.item.module.ItemModule;
import mffs.tile.TileForceFieldProjector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import resonant.lib.prefab.vector.Cuboid;
import universalelectricity.api.vector.Vector3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemModuleRepulsion extends ItemModule
{
	public static final Set<Vector3> repulsionFields = new HashSet<Vector3>();
	private List<Entity> temporaryBlacklist = new ArrayList<Entity>();

	public ItemModuleRepulsion(int id)
	{
		super(id, "moduleRepulsion");
		this.setCost(8);
	}

	@Override
	public boolean onProject(IProjector projector, Set<Vector3> fields)
	{
		final double repulsionVelocity = Math.max(projector.getModuleCount(this) / 20, 1.2);
		final Set<Vector3> field = projector.getCalculatedField();

		Cuboid volume = new Cuboid(projector.getNegativeScale().clone().invert(), projector.getPositiveScale().clone().add(1)).add(new Vector3((TileEntity) projector).add(projector.getTranslation()));
		List<Entity> entities = ((TileEntity) projector).getWorldObj().getEntitiesWithinAABB(Entity.class, volume.toAABB());

		for (Entity entity : entities)
		{
			Vector3 fieldPos = new Vector3(entity).floor();

			if (field.contains(fieldPos))
			{
				if (entity instanceof EntityPlayer)
				{
					EntityPlayer entityPlayer = (EntityPlayer) entity;

					if (entityPlayer.isSneaking())
					{
						IBiometricIdentifier biometricIdentifier = projector.getBiometricIdentifier();

						if (entityPlayer.capabilities.isCreativeMode)
						{
							continue;
						}
						else if (biometricIdentifier != null)
						{
							if (biometricIdentifier.isAccessGranted(entityPlayer.username, Permission.FORCE_FIELD_WARP))
							{
								continue;
							}
						}
					}
				}

				Vector3 repellDirection = new Vector3(entity).difference(fieldPos.clone().translate(0.5)).normalize();
				entity.motionX = repellDirection.x * Math.max(repulsionVelocity, Math.abs(entity.motionX));
				entity.motionY = repellDirection.y * Math.max(repulsionVelocity, Math.abs(entity.motionY));
				entity.motionZ = repellDirection.z * Math.max(repulsionVelocity, Math.abs(entity.motionZ));
				entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ);
				entity.onGround = true;

				if (entity instanceof EntityPlayerMP)
				{
					((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
				}
			}
		}

		/*
		if (((TileEntity) projector).getWorldObj().isRemote && projector.getTicks() % 60 == 0)
		{
			for (Vector3 fieldPos : field)
			{
				if (fieldPos.getBlockID(((TileEntity) projector).getWorldObj()) == 0)
				{
					ModularForceFieldSystem.proxy.renderHologram(((TileEntity) projector).getWorldObj(), fieldPos.clone().translate(0.5), 0.5f, 1, 0.3f, 50, null);
				}
			}
		}*/

		return true;
	}

	@Override
	public boolean onDestroy(IProjector projector, Set<Vector3> field)
	{
		((TileForceFieldProjector) projector).sendFieldToClient();
		return false;
	}

	@Override
	public boolean requireTicks(ItemStack moduleStack)
	{
		return true;
	}
}

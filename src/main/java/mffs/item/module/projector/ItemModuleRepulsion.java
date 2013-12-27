package mffs.item.module.projector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.api.IProjector;
import mffs.api.security.IBiometricIdentifier;
import mffs.api.security.Permission;
import mffs.item.module.ItemModule;
import mffs.tile.TileForceFieldProjector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import universalelectricity.api.vector.Vector3;

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
		double repulsionVelocity = Math.max(projector.getModuleCount(this) / 20, 1.2);
		Set<Vector3> field = projector.getCalculatedField();

		for (Vector3 fieldPos : field)
		{
			List<Entity> entities = ((TileEntity) projector).getWorldObj().getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(fieldPos.intX(), fieldPos.intY(), fieldPos.intZ(), fieldPos.intX() + 1, fieldPos.intY() + 1, fieldPos.intZ() + 1));

			for (Entity entity : entities)
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

				Vector3 differenceFromCenter = new Vector3(entity).difference(fieldPos.clone().translate(0.5));

				entity.posX = entity.lastTickPosX;
				entity.posY = entity.lastTickPosY;
				entity.posZ = entity.lastTickPosZ;
				/*
				 * if (differenceFromCenter.getMagnitude() > 0.49) { entity.motionX *=
				 * -repulsionVelocity; entity.motionY *= -repulsionVelocity; entity.motionZ *=
				 * -repulsionVelocity; } else { System.out.println("TEST"); entity.motionX =
				 * differenceFromCenter.x; entity.motionY = differenceFromCenter.y; entity.motionZ =
				 * differenceFromCenter.z; } entity.moveEntity(entity.motionX, entity.motionY,
				 * entity.motionZ);
				 */

				// entity.moveEntity( differenceFromCenter.x, differenceFromCenter.y,
				// differenceFromCenter.z);
				entity.motionX = 0;
				entity.motionY = 0;
				entity.motionZ = 0;
				entity.onGround = true;

			}

			if (((TileEntity) projector).getWorldObj().isRemote && projector.getTicks() % 60 == 0 && fieldPos.getBlockID(((TileEntity) projector).getWorldObj()) == 0)
			{
				ModularForceFieldSystem.proxy.renderHologram(((TileEntity) projector).getWorldObj(), fieldPos.clone().translate(0.5), 0.5f, 1, 0.3f, 50, null);
			}
		}

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

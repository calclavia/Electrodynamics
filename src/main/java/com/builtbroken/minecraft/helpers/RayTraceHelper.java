package com.builtbroken.minecraft.helpers;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

public class RayTraceHelper
{
	public static MovingObjectPosition raytraceEntities_fromAnEntity(World world, Entity entity, Vec3 error, double reachDistance, boolean collisionFlag)
	{

		MovingObjectPosition pickedEntity = null;
		Vec3 playerPosition = Vec3.createVectorHelper(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
		Vec3 playerLook = entity.getLookVec();

		Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * reachDistance + error.xCoord, playerPosition.yCoord + playerLook.yCoord * reachDistance + error.yCoord, playerPosition.zCoord + playerLook.zCoord * reachDistance + error.zCoord);

		double playerBorder = 1.1 * reachDistance;
		AxisAlignedBB boxToScan = entity.boundingBox.expand(playerBorder, playerBorder, playerBorder);

		List<Entity> entitiesHit = world.getEntitiesWithinAABBExcludingEntity(entity, boxToScan);
		double closestEntity = reachDistance;

		if (entitiesHit == null || entitiesHit.isEmpty())
		{
			return null;
		}
		for (Entity entityHit : entitiesHit)
		{
			if (entityHit != null && entityHit.canBeCollidedWith() && entityHit.boundingBox != null)
			{
				float border = entityHit.getCollisionBorderSize();
				AxisAlignedBB aabb = entityHit.boundingBox.expand(border, border, border);
				MovingObjectPosition hitMOP = aabb.calculateIntercept(playerPosition, playerViewOffset);

				if (hitMOP != null)
				{
					if (aabb.isVecInside(playerPosition))
					{
						if (0.0D < closestEntity || closestEntity == 0.0D)
						{
							pickedEntity = new MovingObjectPosition(entityHit);
							if (pickedEntity != null)
							{
								pickedEntity.hitVec = hitMOP.hitVec;
								closestEntity = 0.0D;
							}
						}
					}
					else
					{
						double distance = playerPosition.distanceTo(hitMOP.hitVec);

						if (distance < closestEntity || closestEntity == 0.0D)
						{
							pickedEntity = new MovingObjectPosition(entityHit);
							pickedEntity.hitVec = hitMOP.hitVec;
							closestEntity = distance;
						}
					}
				}
			}
		}
		return pickedEntity;
	}

	@SuppressWarnings("unchecked")
	public static MovingObjectPosition raytraceEntities(World world, Vec3 start, Vec3 end, double range, boolean collisionFlag, Entity exclude)
	{
		AxisAlignedBB boxToScan = AxisAlignedBB.getBoundingBox(start.xCoord, start.yCoord, start.zCoord, end.xCoord, end.yCoord, end.zCoord);
		List<Entity> entitiesHit = null;
		if (exclude != null)
		{
			entitiesHit = world.getEntitiesWithinAABBExcludingEntity(exclude, boxToScan);
		}
		else
		{
			entitiesHit = world.getEntitiesWithinAABB(Entity.class, boxToScan);
		}
		MovingObjectPosition pickedEntity = null;
		double closestEntity = range;

		if (entitiesHit == null || entitiesHit.isEmpty())
		{
			return null;
		}
		for (Entity entityHit : entitiesHit)
		{
			if (entityHit != null && entityHit.canBeCollidedWith() && entityHit.boundingBox != null)
			{
				float border = entityHit.getCollisionBorderSize();
				AxisAlignedBB aabb = entityHit.boundingBox.expand(border, border, border);
				MovingObjectPosition hitMOP = aabb.calculateIntercept(start, end);

				if (hitMOP != null)
				{
					if (aabb.isVecInside(start))
					{
						if (0.0D < closestEntity || closestEntity == 0.0D)
						{
							pickedEntity = new MovingObjectPosition(entityHit);
							if (pickedEntity != null)
							{
								pickedEntity.hitVec = hitMOP.hitVec;
								closestEntity = 0.0D;
							}
						}
					}
					else
					{
						double distance = start.distanceTo(hitMOP.hitVec);

						if (distance < closestEntity || closestEntity == 0.0D)
						{
							pickedEntity = new MovingObjectPosition(entityHit);
							pickedEntity.hitVec = hitMOP.hitVec;
							closestEntity = distance;
						}
					}
				}
			}
		}
		return pickedEntity;
	}

	public static MovingObjectPosition raytraceBlocks_fromAnEntity(World world, Entity entity, Vec3 error, double reachDistance, boolean collisionFlag)
	{
		Vec3 playerPosition = Vec3.createVectorHelper(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
		Vec3 playerLook = entity.getLookVec();

		Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * reachDistance + error.xCoord, playerPosition.yCoord + playerLook.yCoord * reachDistance + error.yCoord, playerPosition.zCoord + playerLook.zCoord * reachDistance + error.zCoord);
		return raytraceBlocks(world, playerPosition, playerViewOffset, collisionFlag);
	}

	public static MovingObjectPosition raytraceBlocks_fromAnEntity(World world, Entity entity, double reachDistance, boolean collisionFlag)
	{
		Vec3 playerPosition = Vec3.createVectorHelper(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
		Vec3 playerLook = entity.getLookVec();

		Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * reachDistance, playerPosition.yCoord + playerLook.yCoord * reachDistance, playerPosition.zCoord + playerLook.zCoord * reachDistance);
		return raytraceBlocks(world, playerPosition, playerViewOffset, collisionFlag);
	}

	public static MovingObjectPosition raytraceBlocks(World world, Vec3 start, Vec3 end, boolean collisionFlag)
	{
		return world.rayTraceBlocks_do_do(start, end, collisionFlag, !collisionFlag);
	}

	public static Vector3 getPosFromRotation(final Vector3 center, float reachDistance, float yaw, float pitch)
	{
		return center.clone().translate(getLook(yaw, pitch, 1.0F).scale(reachDistance));
	}

	/**
	 * Does a ray trace from start to end vector
	 * 
	 * @param world - world to do the ray trace in
	 * @param start - starting point clear of any collisions from its caster
	 * @param end - end point
	 * @param collisionFlag
	 * @return
	 */
	public static MovingObjectPosition ray_trace_do(final World world, final Vec3 start, final Vec3 end, final float range, boolean collisionFlag)
	{
		MovingObjectPosition hitBlock = raytraceBlocks(world, new Vector3(start).toVec3(), new Vector3(end).toVec3(), collisionFlag);
		MovingObjectPosition hitEntity = raytraceEntities(world, start, end, range, collisionFlag, null);
		if (hitEntity == null)
		{
			return hitBlock;
		}
		else if (hitBlock == null)
		{
			return hitEntity;
		}
		else
		{
			if (hitEntity.hitVec.distanceTo(start) < hitBlock.hitVec.distanceTo(start))
			{
				return hitEntity;
			}
			else
			{
				return hitBlock;
			}
		}
	}

	/**
	 * Does a ray trace from an entities look angle out to a set distance from the entity
	 * 
	 * @param entity - entity who's view angles will be used for finding the start and end points of
	 * the ray
	 * @param e - error(or adjustments) to add to it if this ray is being used for weapon
	 * calculations
	 * @param reachDistance - distance the ray will extend to
	 * @param collisionFlag
	 * @return
	 */
	public static MovingObjectPosition do_rayTraceFromEntity(Entity entity, Vec3 e, double reachDistance, boolean collisionFlag)
	{

		MovingObjectPosition hitBlock = raytraceBlocks_fromAnEntity(entity.worldObj, entity, e, reachDistance, collisionFlag);
		MovingObjectPosition hitEntity = raytraceEntities_fromAnEntity(entity.worldObj, entity, e, reachDistance, collisionFlag);
		if (hitEntity == null)
		{
			return hitBlock;
		}
		else if (hitBlock == null)
		{
			return hitEntity;
		}
		else
		{
			Vec3 playerPosition = Vec3.createVectorHelper(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
			if (hitEntity.hitVec.distanceTo(playerPosition) < hitBlock.hitVec.distanceTo(playerPosition))
			{
				return hitEntity;
			}
			else
			{
				return hitBlock;
			}
		}
	}

	public static Vec3 getLook(Entity entity, float par1)
	{
		float f1;
		float f2;
		float f3;
		float f4;

		if (par1 == 1.0F)
		{
			f1 = MathHelper.cos(-entity.rotationYaw * 0.017453292F - (float) Math.PI);
			f2 = MathHelper.sin(-entity.rotationYaw * 0.017453292F - (float) Math.PI);
			f3 = -MathHelper.cos(-entity.rotationPitch * 0.017453292F);
			f4 = MathHelper.sin(-entity.rotationPitch * 0.017453292F);
			return entity.worldObj.getWorldVec3Pool().getVecFromPool((f2 * f3), f4, (f1 * f3));
		}
		else
		{
			f1 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * par1;
			f2 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * par1;
			f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
			f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
			float f5 = -MathHelper.cos(-f1 * 0.017453292F);
			float f6 = MathHelper.sin(-f1 * 0.017453292F);
			return entity.worldObj.getWorldVec3Pool().getVecFromPool((f4 * f5), f6, (f3 * f5));
		}
	}

	public static Vector3 getLook(float yaw, float pitch, float distance)
	{
		float f1, f2, f3, f4;

		if (distance == 1.0F)
		{
			f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
			f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
			f3 = -MathHelper.cos(-pitch * 0.017453292F);
			f4 = MathHelper.sin(-pitch * 0.017453292F);
			return new Vector3((f2 * f3), f4, (f1 * f3));
		}
		else
		{
			f1 = pitch * distance;
			f2 = yaw * distance;
			f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
			f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
			float f5 = -MathHelper.cos(-f1 * 0.017453292F);
			float f6 = MathHelper.sin(-f1 * 0.017453292F);
			return new Vector3((f4 * f5), f6, (f3 * f5));
		}
	}
}

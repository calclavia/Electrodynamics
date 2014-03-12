package resonantinduction.quantum.gate;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import universalelectricity.api.vector.VectorWorld;

public class QuantumGateManager
{
	private static HashMap<String, Long> playerCooldown = new HashMap<String, Long>();

	protected static boolean moveEntity(Entity currentEntity, final VectorWorld location)
	{
		if (currentEntity != null && location != null && location.world instanceof WorldServer)
		{
			location.world.markBlockForUpdate(location.intX(), location.intY(), location.intZ());

			int dimID = location.world.provider.dimensionId;

			if (currentEntity instanceof EntityPlayerMP)
			{
				if (playerCooldown.get(((EntityPlayerMP) currentEntity).username) == null || (System.currentTimeMillis() - playerCooldown.get(((EntityPlayerMP) currentEntity).username) > 1000))
				{
					EntityPlayerMP player = (EntityPlayerMP) currentEntity;

					if (location.world != currentEntity.worldObj)
					{
						Teleporter dummyTeleporter = new Teleporter((WorldServer) location.world)
						{
							@Override
							public void placeInPortal(Entity teleportEntity, double x, double y, double z, float par8)
							{
								teleportEntity.setLocationAndAngles(location.x, location.y, location.z, teleportEntity.rotationYaw, 0.0F);
								teleportEntity.motionX = teleportEntity.motionY = teleportEntity.motionZ = 0.0D;
								teleportEntity.setSneaking(false);
							}
						};

						player.mcServer.getConfigurationManager().transferPlayerToDimension(player, dimID, dummyTeleporter);
					}
					else
					{
						player.playerNetServerHandler.setPlayerLocation(location.x, location.y, location.z, 0, 0);
					}

					playerCooldown.put(((EntityPlayerMP) currentEntity).username, System.currentTimeMillis());
					return true;
				}
			}
			else
			{
				if (location.world != currentEntity.worldObj)
				{

					currentEntity.worldObj.theProfiler.startSection("changeDimension");
					MinecraftServer minecraftserver = MinecraftServer.getServer();
					int j = currentEntity.dimension;
					WorldServer worldserver = minecraftserver.worldServerForDimension(j);
					WorldServer worldserver1 = minecraftserver.worldServerForDimension(dimID);
					currentEntity.dimension = dimID;

					if (j == 1 && dimID == 1)
					{
						worldserver1 = minecraftserver.worldServerForDimension(0);
						currentEntity.dimension = 0;
					}

					currentEntity.worldObj.removeEntity(currentEntity);
					currentEntity.isDead = false;
					currentEntity.worldObj.theProfiler.startSection("reposition");
					minecraftserver.getConfigurationManager().transferEntityToWorld(currentEntity, j, worldserver, worldserver1);
					currentEntity.worldObj.theProfiler.endStartSection("reloading");
					Entity entity = EntityList.createEntityByName(EntityList.getEntityString(currentEntity), worldserver1);

					if (entity != null)
					{
						entity.copyDataFrom(currentEntity, true);

						if (j == 1 && dimID == 1)
						{
							ChunkCoordinates chunkcoordinates = worldserver1.getSpawnPoint();
							chunkcoordinates.posY = currentEntity.worldObj.getTopSolidOrLiquidBlock(chunkcoordinates.posX, chunkcoordinates.posZ);
							entity.setLocationAndAngles(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, entity.rotationYaw, entity.rotationPitch);
						}

						worldserver1.spawnEntityInWorld(entity);
					}

					currentEntity.isDead = true;
					currentEntity.worldObj.theProfiler.endSection();
					worldserver.resetUpdateEntityTick();
					worldserver1.resetUpdateEntityTick();
					currentEntity.worldObj.theProfiler.endSection();
					return true;
				}

				currentEntity.setPosition(location.x, location.y, location.z);
				return true;
			}

		}
		return false;
	}
}

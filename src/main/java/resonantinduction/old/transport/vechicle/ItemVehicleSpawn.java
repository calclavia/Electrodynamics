package resonantinduction.old.transport.vechicle;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import resonantinduction.core.Settings;

/**
 * Basic item used to spawn a vehicle
 * 
 * @author DarkGuardsman
 */
public class ItemVehicleSpawn extends Item
{
	public ItemVehicleSpawn()
	{
		super(Settings.CONFIGURATION.getItem("ItemCar",Settings.getNextItemID()).getInt());
		this.setUnlocalizedName("Vehicle");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		float playerLook = 1.0F;
		float playerPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * playerLook;
		float playerYaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * playerLook;

		// Find were the current player is looking at
		double lookX = player.prevPosX + (player.posX - player.prevPosX) * playerLook;
		double lookY = player.prevPosY + (player.posY - player.prevPosY) * playerLook + 1.62D - player.yOffset;
		double lookZ = player.prevPosZ + (player.posZ - player.prevPosZ) * playerLook;

		Vec3 start = world.getWorldVec3Pool().getVecFromPool(lookX, lookY, lookZ);

		// Find the vector X blocks away from the player in the same direction as the player is
		// looking
		double searchRange = 5.0D;
		float deltaX = net.minecraft.util.MathHelper.sin(-playerYaw * 0.017453292F - (float) Math.PI) * -net.minecraft.util.MathHelper.cos(-playerPitch * 0.017453292F);
		float deltaY = net.minecraft.util.MathHelper.sin(-playerPitch * 0.017453292F);
		float deltaZ = net.minecraft.util.MathHelper.cos(-playerYaw * 0.017453292F - (float) Math.PI) * -net.minecraft.util.MathHelper.cos(-playerPitch * 0.017453292F);

		Vec3 end = start.addVector(deltaX * searchRange, deltaY * searchRange, deltaZ * searchRange);

		// Check for collision between player look, and player look expanded
		MovingObjectPosition hitObj = world.clip(start, end, true);

		if (hitObj == null)
		{
			return itemStack;
		}
		else
		{
			// Check for collisions using the entities collision box against the spawn location
			Vec3 playerView = player.getLook(playerLook);
			boolean entityInTheWay = false;
			final List<?> entities = world.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.addCoord(playerView.xCoord * searchRange, playerView.yCoord * searchRange, playerView.zCoord * searchRange).expand(1f, 1f, 1f));

			for (int i = 0; i < entities.size(); ++i)
			{
				Entity checkEntity = (Entity) entities.get(i);

				if (checkEntity.canBeCollidedWith())
				{
					float entityBoarderSize = checkEntity.getCollisionBorderSize();
					AxisAlignedBB boundBox = checkEntity.boundingBox.expand(entityBoarderSize, entityBoarderSize, entityBoarderSize);

					if (boundBox.isVecInside(start))
					{
						entityInTheWay = true;
					}
				}
			}
			// IF an entity is in the way return
			if (entityInTheWay)
			{
				return itemStack;
			}
			else
			{
				// Else start to calculate placement
				if (hitObj.typeOfHit == EnumMovingObjectType.TILE)
				{
					int y = hitObj.blockY;

					// Move down if snow
					if (world.getBlockId(hitObj.blockX, hitObj.blockY, hitObj.blockZ) == Block.snow.blockID)
					{
						--y;
					}

					EntityTestCar spawnedEntity = new EntityTestCar(world, hitObj.blockX + 0.5F, y + 1.0F, hitObj.blockZ + 0.5F);

					// Last collision check using the entities collision box
					if (!world.getCollidingBoundingBoxes(spawnedEntity, spawnedEntity.boundingBox.expand(-0.1D, -0.1D, -0.1D)).isEmpty())
					{
						return itemStack;
					}

					if (!world.isRemote)
					{
						world.spawnEntityInWorld(spawnedEntity);
					}

					if (!player.capabilities.isCreativeMode)
					{
						--itemStack.stackSize;
					}
				}

				return itemStack;
			}
		}
	}
}

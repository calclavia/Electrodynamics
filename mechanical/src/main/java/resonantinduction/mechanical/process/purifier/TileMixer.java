package resonantinduction.mechanical.process.purifier;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.mechanical.IMechanical;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.fluid.BlockFluidMixture;
import resonantinduction.mechanical.network.TileMechanical;
import resonantinduction.mechanical.process.Timer;
import universalelectricity.api.vector.Vector3;

/**
 * @author Calclavia
 * 
 */
public class TileMixer extends TileMechanical
{
	public static final long POWER = 500000;
	public static final int PROCESS_TIME = 5 * 20;
	public static final Timer<EntityItem> timer = new Timer<EntityItem>();

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (canWork())
		{
			doWork();
		}
	}

	/**
	 * Can this machine work this tick?
	 * 
	 * @return
	 */
	public boolean canWork()
	{
		return angularVelocity != 0;
	}

	public void doWork()
	{
		boolean didWork = false;

		// Search for an item to "process"
		AxisAlignedBB aabb = AxisAlignedBB.getAABBPool().getAABB(this.xCoord - 1, this.yCoord, this.zCoord - 1, this.xCoord + 2, this.yCoord + 1, this.zCoord + 2);
		List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, aabb);
		Set<EntityItem> processItems = new LinkedHashSet<EntityItem>();

		for (Entity entity : entities)
		{
			/**
			 * Rotate entities around the mixer
			 */
			Vector3 originalPosition = new Vector3(entity);
			Vector3 relativePosition = originalPosition.clone().subtract(new Vector3(this).add(0.5));
			relativePosition.rotate(-angularVelocity, 0, 0);
			Vector3 newPosition = new Vector3(this).add(0.5).add(relativePosition);
			Vector3 difference = newPosition.difference(originalPosition).scale(0.5);

			entity.addVelocity(difference.x, difference.y, difference.z);
			entity.onGround = false;

			if (entity instanceof EntityItem)
			{
				if (MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER, ((EntityItem) entity).getEntityItem()).length > 0)
				{
					processItems.add((EntityItem) entity);
				}
			}
		}

		for (EntityItem processingItem : processItems)
		{
			if (!timer.containsKey(processingItem))
			{
				timer.put(processingItem, PROCESS_TIME);
			}

			if (!processingItem.isDead && new Vector3(this).add(0.5).distance(processingItem) < 2)
			{
				int timeLeft = timer.decrease(processingItem);

				if (timeLeft <= 0)
				{
					if (this.doneWork(processingItem))
					{
						if (--processingItem.getEntityItem().stackSize <= 0)
						{
							processingItem.setDead();
							timer.remove(processingItem);
							processingItem = null;
						}
						else
						{
							processingItem.setEntityItemStack(processingItem.getEntityItem());
							// Reset timer
							timer.put(processingItem, PROCESS_TIME);
						}
					}
				}
				else
				{
					processingItem.delayBeforeCanPickup = 20;
					this.worldObj.spawnParticle("bubble", processingItem.posX, processingItem.posY, processingItem.posZ, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3);
				}

				didWork = true;
			}
			else
			{
				timer.remove(processingItem);
				processingItem = null;
			}
		}

		if (didWork)
		{
			if (this.ticks % 20 == 0)
			{
				this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.PREFIX + "mixer", 0.5f, 1);
			}
		}
	}

	private boolean doneWork(EntityItem entity)
	{
		Vector3 mixPosition = new Vector3(entity.posX, yCoord, entity.posZ);
		Block block = Block.blocksList[mixPosition.getBlockID(worldObj)];

		if (block instanceof BlockFluidMixture)
		{
			ItemStack itemStack = entity.getEntityItem().copy();

			if (((BlockFluidMixture) block).mix(worldObj, mixPosition.intX(), mixPosition.intY(), mixPosition.intZ(), itemStack))
			{
				worldObj.notifyBlocksOfNeighborChange(mixPosition.intX(), mixPosition.intY(), mixPosition.intZ(), mixPosition.getBlockID(worldObj));
				return true;
			}
		}
		else if (worldObj.isAirBlock(mixPosition.intX(), mixPosition.intY(), mixPosition.intZ()) || block.blockID == Block.waterStill.blockID || block.blockID == Block.waterMoving.blockID)
		{
			mixPosition.setBlock(worldObj, ResourceGenerator.getMixture(ResourceGenerator.getName(entity.getEntityItem().getItemDamage())).blockID);
		}

		return false;
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		return from == ForgeDirection.UP || from == ForgeDirection.DOWN;
	}

	@Override
	public boolean inverseRotation(ForgeDirection dir, IMechanical with)
	{
		return false;
	}
}

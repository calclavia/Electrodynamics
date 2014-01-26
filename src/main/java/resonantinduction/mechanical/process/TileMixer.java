package resonantinduction.mechanical.process;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.fluid.TileLiquidMixture;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.TileMechanical;
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
		return angularVelocity > 0;
	}

	public void doWork()
	{
		boolean didWork = false;

		/**
		 * Transform all water blocks into mixture blocks
		 */
		for (int x = -1; x < 1; x++)
		{
			for (int z = -1; z < 1; z++)
			{
				Vector3 checkVector = new Vector3(this).translate(x, 0, z);

				if (checkVector.getBlockID(worldObj) == Block.waterStill.blockID)
				{
					checkVector.setBlock(worldObj, ResonantInduction.blockFluidMixture.blockID, 8, 4);
				}
			}
		}

		// Search for an item to "process"
		AxisAlignedBB aabb = AxisAlignedBB.getAABBPool().getAABB(this.xCoord - 1, this.yCoord, this.zCoord - 1, this.xCoord + 2, this.yCoord + 1, this.zCoord + 2);
		List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, aabb);
		Set<EntityItem> processItems = new LinkedHashSet<EntityItem>();

		for (Entity entity : entities)
		{
			/**
			 * Rotate entities around the mixer
			 */
			double speed = angularVelocity;

			Vector3 originalPosition = new Vector3(entity);
			Vector3 relativePosition = originalPosition.clone().subtract(new Vector3(this).add(0.5));
			relativePosition.rotate(speed, 0, 0);
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
		TileEntity tileEntity = mixPosition.getTileEntity(worldObj);

		if (tileEntity instanceof TileLiquidMixture)
		{
			ItemStack itemStack = entity.getEntityItem().copy();
			if (((TileLiquidMixture) tileEntity).mix(itemStack))
			{
				System.out.println("MIXED");
				worldObj.notifyBlocksOfNeighborChange(mixPosition.intX(), mixPosition.intY(), mixPosition.intZ(), mixPosition.getBlockID(worldObj));
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		return from == ForgeDirection.UP || from == ForgeDirection.DOWN;
	}
}

package resonantinduction.mechanical.process.grinder;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;

import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.RecipeResource;
import resonantinduction.archaic.filter.Timer;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.energy.network.MechanicalNode;
import resonantinduction.mechanical.energy.network.TileMechanical;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.IRotatable;

/**
 * @author Calclavia
 * 
 */
public class TileGrinderWheel extends TileMechanical implements IRotatable
{
	public static final int PROCESS_TIME = 20 * 20;
	/** A map of ItemStacks and their remaining grind-time left. */
	public static final Timer<EntityItem> timer = new Timer<EntityItem>();

	public EntityItem grindingItem = null;

	private final long requiredTorque = 1000;
	private double counter = 0;

	public TileGrinderWheel()
	{
		mechanicalNode = new PacketMechanicalNode(this)
		{
			@Override
			public boolean canConnect(ForgeDirection from, Object source)
			{
				if (getDirection().ordinal() < 2)
				{
					return from.offsetY != 0;
				}

				return getDirection().getRotation(ForgeDirection.UP) == from || getDirection().getRotation(ForgeDirection.DOWN) == from;
			}

			@Override
			public boolean inverseRotation(ForgeDirection dir, MechanicalNode with)
			{
				return !(dir.offsetX > 0 || dir.offsetZ < 0 || dir.offsetY < 0);
			}
		}.setLoad(2);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		counter = Math.max(counter + mechanicalNode.torque, 0);
		doWork();
	}

	/**
	 * Can this machine work this tick?
	 * 
	 * @return
	 */
	public boolean canWork()
	{
		return counter >= requiredTorque;
	}

	public void doWork()
	{
		if (canWork())
		{
			boolean didWork = false;

			if (grindingItem != null)
			{
				if (timer.containsKey(grindingItem) && !grindingItem.isDead && new Vector3(this).add(0.5).distance(grindingItem) < 1)
				{
					int timeLeft = timer.decrease(grindingItem);

					if (timeLeft <= 0)
					{
						if (this.doGrind(grindingItem))
						{
							if (--grindingItem.getEntityItem().stackSize <= 0)
							{
								grindingItem.setDead();
								timer.remove(grindingItem);
								grindingItem = null;
							}
							else
							{
								grindingItem.setEntityItemStack(grindingItem.getEntityItem());
								// Reset timer
								timer.put(grindingItem, PROCESS_TIME);
							}
						}
					}
					else
					{
						grindingItem.delayBeforeCanPickup = 20;

						if (grindingItem.getEntityItem().getItem() instanceof ItemBlock)
						{
							ResonantInduction.proxy.renderBlockParticle(worldObj, new Vector3(grindingItem), new Vector3((Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3), ((ItemBlock) grindingItem.getEntityItem().getItem()).getBlockID(), 1);
						}
						else
						{
							worldObj.spawnParticle("crit", grindingItem.posX, grindingItem.posY, grindingItem.posZ, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3);
						}
					}

					didWork = true;
				}
				else
				{
					timer.remove(grindingItem);
					grindingItem = null;
				}
			}

			if (didWork)
			{
				if (this.ticks % 8 == 0)
				{
					worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.PREFIX + "grinder", 0.5f, 1);
				}

				counter -= requiredTorque;
			}
		}
	}

	public boolean canGrind(ItemStack itemStack)
	{
		// TODO: We don't have a crusher yet, so our grinder currently crushes ores.
		return MachineRecipes.INSTANCE.getOutput(RecipeType.CRUSHER, itemStack).length > 0 || MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER, itemStack).length > 0;
	}

	private boolean doGrind(EntityItem entity)
	{
		ItemStack itemStack = entity.getEntityItem();

		// TODO: Remove this later on when crusher if complete.
		RecipeResource[] results = ArrayUtils.addAll(MachineRecipes.INSTANCE.getOutput(RecipeType.CRUSHER, itemStack), MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER, itemStack));

		for (RecipeResource resource : results)
		{
			ItemStack outputStack = resource.getItemStack();

			if (!this.worldObj.isRemote)
			{
				EntityItem entityItem = new EntityItem(this.worldObj, entity.posX, entity.posY - 1.2, entity.posZ, outputStack);
				entityItem.delayBeforeCanPickup = 20;
				entityItem.motionX = 0;
				entityItem.motionY = 0;
				entityItem.motionZ = 0;
				this.worldObj.spawnEntityInWorld(entityItem);
			}
		}

		return results.length > 0;
	}

	@Override
	public ForgeDirection getDirection()
	{
		if (worldObj != null)
		{
			return ForgeDirection.getOrientation(getBlockMetadata());
		}

		return ForgeDirection.UNKNOWN;
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal(), 3);
	}

}

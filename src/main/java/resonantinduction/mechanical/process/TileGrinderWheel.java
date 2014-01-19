package resonantinduction.mechanical.process;

import java.util.HashMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.RecipeUtils.ItemStackResource;
import resonantinduction.api.recipe.RecipeUtils.Resource;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.network.TileMechanical;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Calclavia
 * 
 */
public class TileGrinderWheel extends TileMechanical
{
	public static final long POWER = 500000;
	public static final int DEFAULT_TIME = 20 * 20;
	/** A map of ItemStacks and their remaining grind-time left. */
	private static final HashMap<EntityItem, Integer> clientTimer = new HashMap<EntityItem, Integer>();
	private static final HashMap<EntityItem, Integer> serverTimer = new HashMap<EntityItem, Integer>();

	public EntityItem grindingItem = null;

	private final long requiredTorque = 10000;
	private long counter = 0;

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		doWork();
	}

	/**
	 * Can this machine work this tick?
	 * 
	 * @return
	 */
	public boolean canWork()
	{
		return (counter = Math.max(counter + getNetwork().getTorque(), 0)) > requiredTorque;
	}

	public void doWork()
	{
		if (canWork())
		{
			boolean didWork = false;

			if (grindingItem != null)
			{
				if (getTimer().containsKey(grindingItem) && !grindingItem.isDead && new Vector3(this).add(0.5).distance(grindingItem) < 1)
				{
					int timeLeft = getTimer().get(grindingItem) - 1;
					getTimer().put(grindingItem, timeLeft);

					if (timeLeft <= 0)
					{
						if (this.doGrind(grindingItem))
						{
							if (--grindingItem.getEntityItem().stackSize <= 0)
							{
								grindingItem.setDead();
								getTimer().remove(grindingItem);
								grindingItem = null;
							}
							else
							{
								grindingItem.setEntityItemStack(grindingItem.getEntityItem());
								// Reset timer
								getTimer().put(grindingItem, DEFAULT_TIME);
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
							this.worldObj.spawnParticle("crit", grindingItem.posX, grindingItem.posY, grindingItem.posZ, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3);
						}
					}

					didWork = true;
				}
				else
				{
					getTimer().remove(grindingItem);
					grindingItem = null;
				}
			}

			if (didWork)
			{
				if (this.ticks % 20 == 0)
				{
					this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.PREFIX + "grinder", 0.5f, 1);
				}

				counter -= requiredTorque;
			}
		}
	}

	public boolean canGrind(ItemStack itemStack)
	{
		return MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack) == null ? false : MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack).length > 0;
	}

	private boolean doGrind(EntityItem entity)
	{
		ItemStack itemStack = entity.getEntityItem();

		Resource[] results = MachineRecipes.INSTANCE.getRecipe(RecipeType.GRINDER, itemStack);

		for (Resource resource : results)
		{
			if (resource instanceof ItemStackResource)
			{
				if (!this.worldObj.isRemote)
				{
					EntityItem entityItem = new EntityItem(this.worldObj, entity.posX, entity.posY, entity.posZ, ((ItemStackResource) resource).itemStack.copy());
					entityItem.delayBeforeCanPickup = 20;
					entityItem.motionX = 0;
					entityItem.motionY = 0;
					entityItem.motionZ = 0;
					this.worldObj.spawnEntityInWorld(entityItem);
				}

				return true;
			}
		}

		return false;
	}

	public static HashMap<EntityItem, Integer> getTimer()
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			return serverTimer;
		}

		return clientTimer;
	}
}

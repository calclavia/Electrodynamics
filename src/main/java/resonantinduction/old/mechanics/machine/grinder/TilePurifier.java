package resonantinduction.old.mechanics.machine.grinder;

import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import resonantinduction.core.Reference;
import resonantinduction.old.mechanics.item.ItemDust;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.TileElectrical;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Calclavia
 * 
 */
public class TilePurifier extends TileElectrical
{
	public static final long POWER = 500000;
	public static final int DEFAULT_TIME = 10 * 20;
	/** A map of ItemStacks and their remaining grind-time left. */
	private static final HashMap<EntityItem, Integer> clientTimer = new HashMap<EntityItem, Integer>();
	private static final HashMap<EntityItem, Integer> serverTimer = new HashMap<EntityItem, Integer>();

	public EntityItem processingItem = null;

	public TilePurifier()
	{
		this.energy = new EnergyStorageHandler(POWER * 2);
	}

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
		// TODO: Add electricity support.
		return true;
	}

	public void doWork()
	{
		boolean didWork = false;

		// Search for an item to "process"
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.xCoord - 1, this.yCoord, this.zCoord - 1, this.xCoord + 1, this.yCoord, this.zCoord + 1);
		List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, aabb);

		for (Entity entity : entities)
		{
			/**
			 * Rotate entities around purifier
			 */
			double speed = 0.1;

			Vector3 originalPosition = new Vector3(this);
			Vector3 relativePosition = originalPosition.clone().subtract(new Vector3(this));
			relativePosition.rotate(speed, 0, 0);
			Vector3 newPosition = new Vector3(this).add(relativePosition);

			if (this.processingItem == null && entity instanceof EntityItem)
			{
				if (((EntityItem) entity).getEntityItem().getItem() instanceof ItemDust)
				{
					this.processingItem = (EntityItem) entity;
				}
			}
		}

		if (processingItem != null)
		{
			if (getTimer().containsKey(processingItem) && !processingItem.isDead && new Vector3(this).add(0.5).distance(processingItem) < 1)
			{
				int timeLeft = getTimer().get(processingItem) - 1;
				getTimer().put(processingItem, timeLeft);

				if (timeLeft <= 0)
				{
					if (this.doneWork(processingItem))
					{
						if (--processingItem.getEntityItem().stackSize <= 0)
						{
							processingItem.setDead();
							getTimer().remove(processingItem);
							processingItem = null;
						}
						else
						{
							processingItem.setEntityItemStack(processingItem.getEntityItem());
							// Reset timer
							getTimer().put(processingItem, DEFAULT_TIME);
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
				getTimer().remove(processingItem);
				processingItem = null;
			}
		}

		if (didWork)
		{
			if (this.ticks % 20 == 0)
			{
				this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.PREFIX + "grinder", 0.5f, 1);
			}

			this.energy.extractEnergy(POWER / 20, true);
		}
	}

	private boolean doneWork(EntityItem entity)
	{
		ItemStack itemStack = entity.getEntityItem();
		entity.setDead();
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

package resonantinduction.mechanical.process.grinder;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.IRotatable;
import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.lib.prefab.CustomDamageSource;
import resonant.lib.prefab.vector.Cuboid;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInduction.RecipeType;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.core.Timer;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.energy.grid.TileMechanical;
import universalelectricity.api.vector.Vector3;

/**
 * @author Calclavia
 */
public class TileGrindingWheel extends TileMechanical implements IRotatable
{
	public static final int PROCESS_TIME = 20 * 20;
	/**
	 * A map of ItemStacks and their remaining grind-time left.
	 */
	public static final Timer<EntityItem> timer = new Timer();

	public EntityItem grindingItem = null;

	private final long requiredTorque = 250;
	private double counter = 0;

	public TileGrindingWheel()
	{
		super(Material.rock);
		mechanicalNode = new GrinderNode(this).setLoad(2);
		bounds = new Cuboid(0.05f, 0.05f, 0.05f, 0.95f, 0.95f, 0.95f);
		isOpaqueCube = false;
		normalRender = false;
		customItemRender = true;
		rotationMask = Byte.parseByte("111111", 2);
		textureName = "material_steel_dark";
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		counter = Math.max(counter + Math.abs(mechanicalNode.torque), 0);
		doWork();
	}

	@Override
	public void collide(Entity entity)
	{
		if (entity instanceof EntityItem)
		{
			((EntityItem) entity).age--;
		}

		if (canWork())
		{
			if (entity instanceof EntityItem)
			{
				if (canGrind(((EntityItem) entity).getEntityItem()))
				{
					if (grindingItem == null)
					{
						grindingItem = (EntityItem) entity;
					}

					if (!TileGrindingWheel.timer.containsKey((EntityItem) entity))
					{
						TileGrindingWheel.timer.put((EntityItem) entity, TileGrindingWheel.PROCESS_TIME);
					}
				}
				else
				{
					entity.setPosition(entity.posX, entity.posY - 1.2, entity.posZ);
				}
			}
			else
			{
				entity.attackEntityFrom(new CustomDamageSource("grinder", this), 2);
			}

		}

		if (mechanicalNode.getAngularSpeed() != 0)
		{
			// Move entity based on the direction of the block.
			ForgeDirection dir = getDirection();
			dir = ForgeDirection.getOrientation(!(dir.ordinal() % 2 == 0) ? dir.ordinal() - 1 : dir.ordinal()).getOpposite();
			double speed = mechanicalNode.getAngularSpeed() / 20;
			double speedX = dir.offsetX * speed;
			double speedZ = dir.offsetZ * speed;
			double speedY = Math.random() * speed;
			if (Math.abs(speedX) > 1)
			{
				speedX = speedX > 0 ? 1 : -1;
			}
			if (Math.abs(speedZ) > 1)
			{
				speedZ = speedZ > 0 ? 1 : -1;
			}
			if (Math.abs(speedZ) > 1)
			{
				speedY = speedY > 0 ? 1 : -1;
			}
			entity.addVelocity(speedX, speedY, speedZ);
		}
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
		return MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name(), itemStack).length > 0;
	}

	private boolean doGrind(EntityItem entity)
	{
		ItemStack itemStack = entity.getEntityItem();

		RecipeResource[] results = MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name(), itemStack);

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

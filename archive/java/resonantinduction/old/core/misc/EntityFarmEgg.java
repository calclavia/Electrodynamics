package resonantinduction.old.core.misc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/**
 * Override version of the vanilla egg to allow additional bird types to exist with the same item
 * 
 * @author DarkGuardsman
 */
public class EntityFarmEgg extends EntityThrowable
{
	protected int spawnEntityId = 0;

	public EntityFarmEgg(World world)
	{
		super(world);
		this.spawnEntityId = 0;
	}

	public EntityFarmEgg(World world, int id)
	{
		super(world);
		this.spawnEntityId = id;
	}

	public EntityFarmEgg(World world, EntityLivingBase entityLivingBase, int id)
	{
		super(world, entityLivingBase);
		this.spawnEntityId = id;
	}

	public EntityFarmEgg(World world, double x, double y, double z, int id)
	{
		super(world, x, y, z);
		this.spawnEntityId = id;
	}

	/** Called when this EntityThrowable hits a block or entity. */
	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		if (mop.entityHit != null)
		{
			mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
		}

		if (!this.worldObj.isRemote && this.rand.nextInt(8) == 0)
		{
			EntityAnimal animal;
			switch (spawnEntityId)
			{
				default:
					animal = new EntityChicken(this.worldObj);
					break;
			}
			if (animal != null)
			{
				animal.setGrowingAge(-24000);
				animal.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
				this.worldObj.spawnEntityInWorld(animal);
			}
		}

		for (int j = 0; j < 8; ++j)
		{
			this.worldObj.spawnParticle("snowballpoof", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		}

		if (!this.worldObj.isRemote)
		{
			this.setDead();
		}
	}
}

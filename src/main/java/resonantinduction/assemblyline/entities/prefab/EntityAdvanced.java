package resonantinduction.assemblyline.entities.prefab;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Extended version of the entity, used in the case that an entity needs to be created that can't
 * extend entity living base
 * 
 * @author DarkGuardsman */
public abstract class EntityAdvanced extends Entity
{
    protected float maxDamage = 50;
    protected long ticks = 1;
    protected static final int DAMAGE_ID = 6, HIT_TICK_ID = 7, ROLL_DIRECTION_ID = 8, ROLL_AMP_ID = 9, FORWARD_DIRECTION_ID = 10;

    public EntityAdvanced(World world)
    {
        super(world);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    protected void entityInit()
    {
        this.dataWatcher.addObject(DAMAGE_ID, Float.valueOf(1.0F));
        this.dataWatcher.addObject(HIT_TICK_ID, Integer.valueOf(0));
        this.dataWatcher.addObject(ROLL_DIRECTION_ID, Integer.valueOf(0));
        this.dataWatcher.addObject(ROLL_AMP_ID, Integer.valueOf(0));
        this.dataWatcher.addObject(FORWARD_DIRECTION_ID, Integer.valueOf(0));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (this.ticks++ >= Long.MAX_VALUE - 10)
        {
            this.ticks = 1;
        }
        if (this.worldObj.isRemote)
        {
            this.updateAnimation();
        }
        if (this.getTimeSinceHit() > 0)
        {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }
    }

    /** Don't do any client side calls but rather update variables that control renders. For example
     * rotation of the model */
    public void updateAnimation()
    {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.setHealth(nbt.getFloat("Health"));

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setFloat("Health", this.getHealth());

    }

    public final float getHealth()
    {
        return this.dataWatcher.getWatchableObjectFloat(DAMAGE_ID);
    }

    public void setHealth(float hp)
    {
        this.dataWatcher.updateObject(DAMAGE_ID, Float.valueOf(MathHelper.clamp_float(hp, 0.0F, this.getMaxHealth())));
    }

    /** Max damage this entity can take */
    public float getMaxHealth()
    {
        return this.maxDamage;
    }

    /** Sets the time to count down from since the last time entity was hit. */
    public void setTimeSinceHit(int par1)
    {
        this.dataWatcher.updateObject(HIT_TICK_ID, Integer.valueOf(par1));
    }

    /** Gets the time since the last hit. */
    public int getTimeSinceHit()
    {
        return this.dataWatcher.getWatchableObjectInt(HIT_TICK_ID);
    }

    /** Sets the rolling amplitude the cart rolls while being attacked. */
    public void setRollingAmplitude(int par1)
    {
        this.dataWatcher.updateObject(ROLL_AMP_ID, Integer.valueOf(par1));
    }

    /** Gets the rolling amplitude the cart rolls while being attacked. */
    public int getRollingAmplitude()
    {
        return this.dataWatcher.getWatchableObjectInt(ROLL_AMP_ID);
    }

    /** Sets the rolling direction the cart rolls while being attacked. Can be 1 or -1. */
    public void setRollingDirection(int par1)
    {
        this.dataWatcher.updateObject(ROLL_DIRECTION_ID, Integer.valueOf(par1));
    }

    /** Gets the rolling direction the cart rolls while being attacked. Can be 1 or -1. */
    public int getRollingDirection()
    {
        return this.dataWatcher.getWatchableObjectInt(ROLL_DIRECTION_ID);
    }

    /** Sets the forward direction of the entity. */
    public void setForwardDirection(int par1)
    {
        this.dataWatcher.updateObject(FORWARD_DIRECTION_ID, Integer.valueOf(par1));
    }

    /** Gets the forward direction of the entity. */
    public int getForwardDirection()
    {
        return this.dataWatcher.getWatchableObjectInt(FORWARD_DIRECTION_ID);
    }

}

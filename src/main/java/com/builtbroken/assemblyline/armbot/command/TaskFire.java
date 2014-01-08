package com.builtbroken.assemblyline.armbot.command;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.assemblyline.api.IArmbot;
import com.builtbroken.assemblyline.api.coding.args.ArgumentFloatData;
import com.builtbroken.assemblyline.armbot.TaskBaseArmbot;
import com.builtbroken.assemblyline.armbot.TaskBaseProcess;
import com.builtbroken.common.Pair;
import com.builtbroken.common.science.units.UnitHelper;

public class TaskFire extends TaskBaseArmbot
{

    private static final float MIN_ACTUAL_PITCH = -80;
    private static final float MAX_ACTUAL_PITCH = 80;

    private float actualYaw;
    private float actualPitch;
    private float velocity;
    private Vector3 finalVelocity;

    public TaskFire()
    {
        super("throw");
        this.args.add(new ArgumentFloatData("velocity", 1.0f, 2.5f, 1.0f));
    }

    @Override
    public ProcessReturn onMethodCalled()
    {
        if (super.onMethodCalled() == ProcessReturn.CONTINUE)
        {
            this.velocity = UnitHelper.tryToParseFloat(this.getArg("velocity"));
            if (this.velocity > 2.5f)
            {
                this.velocity = 2.5f;
            }
            if (this.velocity < 0.125f)
            {
                this.velocity = 1f;
            }

            this.actualYaw = (float) ((IArmbot) this.program.getMachine()).getRotation().x;
            this.actualPitch = ((MAX_ACTUAL_PITCH - MIN_ACTUAL_PITCH) * ((float) ((IArmbot) this.program.getMachine()).getRotation().y / 60f)) + MIN_ACTUAL_PITCH;

            double x, y, z;
            double yaw, pitch;
            yaw = Math.toRadians(actualYaw);
            pitch = Math.toRadians(actualPitch);
            // yaw = actualYaw;
            // pitch = actualPitch;

            x = -Math.sin(yaw) * Math.cos(pitch);
            y = Math.sin(pitch);
            z = Math.cos(yaw) * Math.cos(pitch);

            this.finalVelocity = new Vector3(x, y, z);
            Random random = new Random(System.currentTimeMillis());
            this.finalVelocity.x *= (1f - (1f / 200f)) + (random.nextFloat() * (1f / 100f));
            this.finalVelocity.y *= (1f - (1f / 200f)) + (random.nextFloat() * (1f / 100f));
            this.finalVelocity.z *= (1f - (1f / 200f)) + (random.nextFloat() * (1f / 100f));

            this.finalVelocity.scale(velocity);
            return ProcessReturn.CONTINUE;
        }
        return ProcessReturn.GENERAL_ERROR;
    }

    @Override
    public ProcessReturn onUpdate()
    {
        if (super.onUpdate() == ProcessReturn.CONTINUE)
        {
            if (this.finalVelocity == null) // something went wrong
            {
                this.finalVelocity = new Vector3(0, 0, 0);
            }
            if (((IArmbot) this.program.getMachine()).getHeldObject() != null)
            {
                Entity held = null;
                Object obj = ((IArmbot) this.program.getMachine()).getHeldObject();
                Pair<World, Vector3> location = this.program.getMachine().getLocation();
                if (obj instanceof Entity)
                {
                    held = (Entity) obj;
                }
                if (held != null)
                {
                    location.left().playSound(location.right().x, location.right().y, location.right().z, "random.bow", velocity, 2f - (velocity / 4f), true);
                    if (held instanceof EntityItem)
                    {
                        EntityItem item = (EntityItem) held;
                        ItemStack stack = item.getEntityItem();
                        ItemStack thrown = stack.copy();
                        thrown.stackSize = 1;
                        if (item.getEntityItem().stackSize > 0)
                        {
                            stack.stackSize--;
                            item.setEntityItemStack(stack);
                        }
                        else
                        {
                            ((IArmbot) this.program.getMachine()).dropHeldObject();
                            if (!location.left().isRemote)
                            {
                                location.left().removeEntity(held);
                            }
                        }
                        if (item.getEntityItem().itemID == Item.arrow.itemID)
                        {
                            EntityArrow arrow = new EntityArrow(location.left(), ((IArmbot) this.program.getMachine()).getHandPos().x, ((IArmbot) this.program.getMachine()).getHandPos().y, ((IArmbot) this.program.getMachine()).getHandPos().z);
                            arrow.motionX = this.finalVelocity.x;
                            arrow.motionY = this.finalVelocity.y;
                            arrow.motionZ = this.finalVelocity.z;
                            if (!location.left().isRemote)
                            {
                                location.left().spawnEntityInWorld(arrow);
                            }
                        }
                        else
                        {
                            EntityItem item2 = new EntityItem(location.left(), ((IArmbot) this.program.getMachine()).getHandPos().x, ((IArmbot) this.program.getMachine()).getHandPos().y, ((IArmbot) this.program.getMachine()).getHandPos().z, thrown);
                            item2.motionX = this.finalVelocity.x;
                            item2.motionY = this.finalVelocity.y;
                            item2.motionZ = this.finalVelocity.z;
                            if (!location.left().isRemote)
                            {
                                location.left().spawnEntityInWorld(item2);
                            }
                        }
                    }
                    else
                    {
                        ((IArmbot) this.program.getMachine()).dropHeldObject();
                        held.motionX = this.finalVelocity.x;
                        held.motionY = this.finalVelocity.y;
                        held.motionZ = this.finalVelocity.z;
                    }
                }
            }
            return ProcessReturn.DONE;
        }
        return ProcessReturn.GENERAL_ERROR;

    }

    @Override
    public TaskBaseProcess loadProgress(NBTTagCompound taskCompound)
    {
        super.loadProgress(taskCompound);
        this.actualYaw = taskCompound.getFloat("fireYaw");
        this.actualPitch = taskCompound.getFloat("firePitch");
        this.velocity = taskCompound.getFloat("fireVelocity");
        this.finalVelocity = new Vector3();
        this.finalVelocity.x = taskCompound.getDouble("fireVectorX");
        this.finalVelocity.y = taskCompound.getDouble("fireVectorY");
        this.finalVelocity.z = taskCompound.getDouble("fireVectorZ");
        return this;
    }

    @Override
    public NBTTagCompound saveProgress(NBTTagCompound taskCompound)
    {
        super.saveProgress(taskCompound);
        taskCompound.setFloat("fireYaw", this.actualYaw);
        taskCompound.setFloat("firePitch", this.actualPitch);
        taskCompound.setFloat("fireVelocity", this.velocity);
        if (this.finalVelocity != null)
        {
            taskCompound.setDouble("fireVectorX", this.finalVelocity.x);
            taskCompound.setDouble("fireVectorY", this.finalVelocity.y);
            taskCompound.setDouble("fireVectorZ", this.finalVelocity.z);
        }
        return taskCompound;
    }

    @Override
    public String toString()
    {
        return super.toString() + " " + Float.toString(this.velocity);
    }

    @Override
    public TaskBaseProcess clone()
    {
        return new TaskFire();
    }
}

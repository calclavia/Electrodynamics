package com.builtbroken.assemblyline.api;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.assemblyline.api.coding.IProgrammableMachine;

/** Should be used to interact with the armbot and not to create a new armbot */
public interface IArmbot extends Cloneable, IProgrammableMachine
{
    /** Location of the hand, or working location of the object */
    public Vector3 getHandPos();

    /** Gets the rotation as a Vector2 (X - Yaw, Y - pitch) */
    public Vector2 getRotation();

    /** Forces the rotation to the two angles */
    public void setRotation(int yaw, int pitch);

    /** Ask the armbot to rotate to face the given direction. Some bots may not support all angles
     * 
     * @return true if the bot will comply. May return false if it can't */
    public boolean moveArmTo(int yaw, int pitch);

    /** Ask the armbot to rotate to face the given direction. Some bots may not support up and down
     * 
     * @param direction - direction
     * @return true if the bot will comply. May return false if it can't */
    public boolean moveTo(ForgeDirection direction);

    /** Object currently held. In some cases this can be a list or array but is suggest to only be
     * one object */
    public Object getHeldObject();

    /** Adds an entity to the Armbot's grab list. Entity or ItemStack
     * 
     * @entity - object to grab, can be anything though is suggest to be an entity or itemstack
     * @return - true if the bot has grabbed the object */
    public boolean grabObject(Object entity);

    /** Drops the current held object. Use getGrabbedObject to make sure this is the object to drop. */
    public boolean dropHeldObject();

    /** Same as deleting the object */
    public boolean clear(Object object);

}

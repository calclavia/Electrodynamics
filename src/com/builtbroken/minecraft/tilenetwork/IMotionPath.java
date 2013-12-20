package com.builtbroken.minecraft.tilenetwork;

import net.minecraft.entity.Entity;
import universalelectricity.core.vector.Vector3;

/** Use by tiles to control the path of motion of an item threw a tile network such as items pipes
 * 
 * @author DarkGuardsman */
public interface IMotionPath
{

    /** Gets the motion applied to the entity while its on the tile **/
    public Vector3 getMotion(Entity entity);

    /** Can the path controller move the entity over this tile. Make sure to check the position of
     * the tile as the controller doesn't know the range of control of the tile. Though it does
     * limit itself to blocks around the entity. So if your tile only effects entities above it
     * within its bound then make sure the tile is inside those bounds
     * 
     * @param entity - entity in question
     * @param from - direction the entity came from
     * @return true if it can, false if something is wrong like no power, or solid side */
    public boolean canMoveEntity(Entity entity);
}

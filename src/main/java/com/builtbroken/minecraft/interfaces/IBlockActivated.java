package com.builtbroken.minecraft.interfaces;

import net.minecraft.entity.player.EntityPlayer;

/** Used re relay an activation event from a block or entity to a tile or another entity */
public interface IBlockActivated
{
    /** Called when activated. Use angle from tile to get side. and getHeldItem to get the item the
     * player is using */
    public boolean onActivated(EntityPlayer entityPlayer);
}
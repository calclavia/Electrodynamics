package com.builtbroken.minecraft.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;

/** Simple way to control the read-out display over several tools when they are used on the
 * tileEntity
 * 
 * @author DarkGuardsman */
public interface IToolReadOut
{
    /** Grabs the message displayed to the user on right click of the machine with the given tool
     * 
     * @param user
     * @param side - may not work correctly yet but should give you a side
     * @return - a string to be displayed to the player for a reading. automatically adds ReadOut:
     * to the beginning */
    public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool);

    public static enum EnumTools
    {
        PIPE_GUAGE(),
        MULTI_METER();

        public static EnumTools get(int meta)
        {
            if (meta < EnumTools.values().length)
            {
                return EnumTools.values()[meta];
            }
            return null;
        }
    }
}

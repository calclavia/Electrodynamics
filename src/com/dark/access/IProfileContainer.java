package com.dark.access;

/** Applied to tileEntities that contain an access profile that describes how the tile interacts with
 * users
 * 
 * @author DarkGuardsman */
public interface IProfileContainer
{
    /** Return the active profile of the machine. When calling this avoid editing the profile */
    public AccessProfile getAccessProfile();

    /** Strait up yes or no can this user access the tile. Any future checks should be done after the
     * user has accessed the machine */
    public boolean canAccess(String username);
}

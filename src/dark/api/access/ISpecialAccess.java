package dark.api.access;

import java.util.List;

/** Used by any object that needs to restrict access to it by a set of users or groups. Make sure to
 * always use the default groups(user,admin,owner) so that things work smoothly.
 * 
 * @author DarkGuardsman */
public interface ISpecialAccess
{
    /** Gets the user access instance */
    public AccessUser getUserAccess(String username);

    /** gets the user access list for the machine */
    public List<AccessUser> getUsers();

    /** sets the players access level in the access map. Make sure to remove the old user first. This
     * can also be used to remove users if group is set to null. */
    public boolean setUserAccess(String username, AccessGroup group, boolean save);

    /** Sets the players access by using a completed AccessUser instance. Make sure to set its group
     * if there is none. As well remove the old user first. */
    public boolean setUserAccess(AccessUser user, AccessGroup group);

    /** Get a group by name */
    public AccessGroup getGroup(String name);

    /** Get the master owner group */
    public AccessGroup getOwnerGroup();

    /** Get all groups linked this */
    public List<AccessGroup> getGroups();

    /** Add a group to the group list
     * 
     * @return */
    public boolean addGroup(AccessGroup group);

}

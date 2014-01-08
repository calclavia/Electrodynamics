package com.builtbroken.minecraft.access;

/** Constants that represent nodes by which machines and entities used in combination with
 * ISpecialAccess to limit users on what they can do. These nodes should be used in the same way by
 * all machines, entities, and other mods. Too change the meaning of the node will make it difficult
 * to offer universal meaning for all machines. As well would create the need to add a per node per
 * machine per group access list making it more complicated for users to use.
 *
 * @author DarkGuardsman */
public class Nodes
{
    /*
     * Rules for using nodes with groups and your machine:
     *
     * Keep everything the same.
     *   Eg: Open should be as simple as opening the gui and no more
     *
     * Enable is not the same as on
     *   Eg: you can disable a machine preventing users from using it
     *   Eg: When enabled the machine can still be turned off
     *
     * Lock node automatically includes unlock node but not the other way around
     *
     *
     * Machine nodes override inv node as inv nodes are only designed for containers.
     *  Machines nodes are global for all guis inside the machine
     *
     *
     *  Groups do not need there own nodes. Group.user, Group.owner, Group.admin are designed to flag the default groups.
     *
     *
     * Your machine must always have a group that at least has a owner group. This group should be flagged with Group.owner but is not required.
     *
     *
     *
     */

    //Inventory only nodes, overrided by machine nodes
    public static final String INV_OPEN_NODE = "inv.open";
    public static final String INV_TAKE_NODE = "inv.take";
    public static final String INV_GIVE_NODE = "inv.give";
    public static final String INV_EDIT_NODE = "inv.edit";
    public static final String INV_CHANGE_NODE = "inv.change";
    public static final String INV_LOCK_NODE = "inv.lock";
    public static final String INV_UNLOCK_NODE = "inv.unlock";
    public static final String INV_DISABLE_NODE = "inv.disable";
    public static final String INV_ENABLE_NODE = "inv.enable";

    //Master machines nodes, overrides all lower nodes of the same type
    public static final String MACHINE_OPEN_NODE = "machine.open";
    public static final String MACHINE_LOCK_NODE = "machine.lock";
    public static final String MACHINE_UNLOCK_NODE = "machine.unlock";
    public static final String MACHINE_ENABLE_NODE = "machine.enable";
    public static final String MACHINE_DISABLE_NODE = "machine.disable";
    public static final String MACHINE_TURN_ON_NODE = "machine.on";
    public static final String MACHINE_TURN_OFF_NODE = "machine.off";
    public static final String MACHINE_CONFIG_NODE = "machine.config";
    public static final String MACHINE_UPGRADE_NODE = "machine.upgrade";
    public static final String MACHINE_DOWNGRADE_NODE = "machine.downgrade";

    //Group nodes, these are almost always held by only admins and owners
    public static final String GROUP_CREATE_NODE = "group.create";
    public static final String GROUP_DEL_NODE = "group.del";
    public static final String GROUP_EDIT_NODE = "group.edit";//Still bound by other nodes
    public static final String GROUP_ADD_NODE = "group.add";
    public static final String GROUP_ADD_USER_NODE = "group.add.user";
    public static final String GROUP_ADD_ENTITY_NODE = "group.add.entity";
    public static final String GROUP_ADD_NODE_NODE = "group.add.node";
    public static final String GROUP_REMOVE_NODE = "group.remove";
    public static final String GROUP_REMOVE_USER_NODE = "group.remove.user";
    public static final String GROUP_REMOVE_ENTITY_NODE = "group.remove.entity";
    public static final String GROUP_REMOVE_NODE_NODE = "group.remove.node";

    //Nodes for editing users inside of a group
    public static final String USER_EDIT_NODE = "user.edit";
    public static final String USER_CHANGE_GROUP_NODE = "user.change.group";
    public static final String USER_ADD_NODE = "user.add.node";
    public static final String USER_REMOVE_NODE = "user.remove.node";

    //Prefab group nodes, designed to be placed on the master group of the type
    public static final String GROUP_OWNER_NODE = "group.owner";
    public static final String GROUP_ADMIN_NODE = "group.admin";
    public static final String GROUP_USER_NODE = "group.user";
}

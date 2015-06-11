package com.resonant.core.access;

/**
 * Constants that represent permissions by which machines and entities used in combination with
 * ISpecialAccess to limit users on what they can do. These permissions should be used in the same
 * way by
 * all machines, entities, and other mods. Too change the meaning of the node will make it difficult
 * to offer universal meaning for all machines. As well would create the need to add a per node per
 * machine per group access list making it more complicated for users to use.
 * @author DarkGuardsman
 */
public class Permissions {
	public static final Permission root = new Permission("root");

	// Inventory only permissions, overridden by machine permissions
	public static final Permission inventory = root.addChild("inventory");
	public static final Permission inventoryOpen = inventory.addChild("open");
	public static final Permission inventoryInput = inventory.addChild("input");
	public static final Permission inventoryOutput = inventory.addChild("output");
	public static final Permission inventoryModify = inventory.addChild("modify");
	public static final Permission inventoryLock = inventory.addChild("lock");
	public static final Permission inventoryUnlock = inventory.addChild("unlock");
	public static final Permission inventoryDisable = inventory.addChild("disable");
	public static final Permission inventoryEnable = inventory.addChild("enable");

	// Master machines permissions, overrides all lower permissions of the same type
	public static final Permission machine = root.addChild("machine");
	public static final Permission machineOpen = machine.addChild("open");
	public static final Permission machineLock = machine.addChild("lock");
	public static final Permission machineUnlock = machine.addChild("unlock");
	public static final Permission machineEnable = machine.addChild("enable");
	public static final Permission machineDisable = machine.addChild("disable");
	public static final Permission machineTurnOn = machine.addChild("on");
	public static final Permission machineTurnOff = machine.addChild("off");
	public static final Permission machineConfigure = machine.addChild("config");
	public static final Permission machineUpgrade = machine.addChild("upgrade");
	public static final Permission machineDowngrade = machine.addChild("downgrade");

	// Group permissions, these are almost always held by only admins and owners
	public static final Permission profile = root.addChild("profile");
	public static final Permission profileAddGroup = profile.addChild("addGroup");
	public static final Permission profileRemoveGroup = profile.addChild("removeGroup");
	public static final Permission profileModifyGroup = profile.addChild("modifyGroup");

	public static final Permission group = root.addChild("group");
	public static final Permission groupPermission = group.addChild("permission");
	public static final Permission groupPermissionAdd = groupPermission.addChild("add");
	public static final Permission groupPermissionRemove = groupPermission.addChild("remove");

	public static final Permission groupUser = group.addChild("user");
	public static final Permission groupUserAdd = groupUser.addChild("add");
	public static final Permission groupUserRemove = groupUser.addChild("remove");
	public static final Permission groupUserModify = groupUser.addChild("modify");

	public static final Permission groupUserPermission = group.addChild("permission");
	public static final Permission groupUserPermissionAdd = groupUserPermission.addChild("add");
	public static final Permission groupUserPermissionRemove = groupUserPermission.addChild("remove");

	public static final Permission groupEntity = group.addChild("entity");
	public static final Permission GROUP_ADD_ENTITY = groupEntity.addChild("add");
	public static final Permission GROUP_REMOVE_ENTITY = groupEntity.addChild("remove");

	// Default group id getNodes for the entire profile
	public static final Permission PROFILE_OWNER = profile.addChild("owner");
	public static final Permission PROFILE_ADMIN = profile.addChild("admin");
	public static final Permission PROFILE_USER = profile.addChild("user");

	// Default user id getNodes for a single group
	public static final Permission GROUP_OWNER = group.addChild("owner");
	public static final Permission GROUP_ADMIN = group.addChild("admin");
	public static final Permission GROUP_USER = group.addChild("user");

	/**
	 * Gets the permission instance with the specific name.
	 * @param name - The full name of the permission.
	 * @return
	 */
	public static Permission find(String name) {
		String[] permNames = name.split("\\.");

		Permission currentPerm = root;

		if (permNames.length > 0) {
			for (int i = 1; i < permNames.length; i++) {
				Permission checkPerm = currentPerm.find(permNames[i]);

				if (checkPerm != null) {
					currentPerm = checkPerm;
				} else {
					break;
				}
			}

			return currentPerm;
		}

		return null;
	}

}

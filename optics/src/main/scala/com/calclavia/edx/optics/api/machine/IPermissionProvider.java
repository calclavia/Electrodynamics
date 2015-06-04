package com.calclavia.edx.optics.api.machine;

import com.resonant.core.access.Permission;

/**
 * Used by tiles that provide permissions.
 * @author Calclavia
 */
public interface IPermissionProvider {
	/**
	 * Does this field matrix provide a specific permission?
	 */
	public boolean hasPermission(String id, Permission permission);
}

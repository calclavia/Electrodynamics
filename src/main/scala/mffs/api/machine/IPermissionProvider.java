package mffs.api.machine;

import com.mojang.authlib.GameProfile;
import com.resonant.access.Permission;

/**
 * Used by tiles that provide permissions.
 * @author Calclavia
 */
public interface IPermissionProvider {
	/**
	 * Does this field matrix provide a specific permission?
	 */
	public boolean hasPermission(GameProfile profile, Permission permission);
}

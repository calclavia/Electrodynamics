package mffs.security.access;

public class MFFSPermissions
{
    // TODO: FIX NAMES
    /** Force Field Wrap - Allows a player to go through force fields. */
    public static final MFFSPermissions FORCE_FIELD_WARP = new MFFSPermissions(0, "warp");
    /** Place Access - Allows to open GUIs and activate blocks. */
    public static final MFFSPermissions BLOCK_ALTER = new MFFSPermissions(1, "blockPlaceAccess");
    /** Block Access - Allows block access and opening GUIs. */
    public static final MFFSPermissions BLOCK_ACCESS = new MFFSPermissions(2, "blockAccess");
    /** Configure - Allows to configure biometric identifiers. */
    public static final MFFSPermissions SECURITY_CENTER_CONFIGURE = new MFFSPermissions(3, "configure");
    /** Bypass Confiscation - Allows the bypassing of interdiction matrix confiscation. */
    public static final MFFSPermissions BYPASS_INTERDICTION_MATRIX = new MFFSPermissions(4, "bypassDefense");
    /** Remote Control - Allows the usage of a remote control to open GUIs remotely. */
    public static final MFFSPermissions DEFENSE_STATION_CONFISCATION = new MFFSPermissions(5, "bypassConfiscation");
    /** Remote Control - Allows player to remotely control blocks with the remote. */
    public static final MFFSPermissions REMOTE_CONTROL = new MFFSPermissions(6, "remoteControl");

    private static MFFSPermissions[] LIST;

    public final int id;
    public final String name;

    public MFFSPermissions(int id, String name)
    {
        this.id = id;
        this.name = name;

        if (LIST == null)
        {
            LIST = new MFFSPermissions[7];
        }

        LIST[this.id] = this;
    }

    public static MFFSPermissions getPermission(int id)
    {
        if (id < LIST.length && id >= 0)
        {
            return LIST[id];
        }

        return null;
    }

    public static MFFSPermissions[] getPermissions()
    {
        return LIST;
    }
}
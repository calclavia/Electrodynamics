package resonantinduction.old.lib.interfaces;

/**
 * Applied to devices that have the option to run without power. Normally this option is only shown
 * to creative mode players
 * 
 * @author DarkGuardsman
 */
public interface IPowerLess
{
	/** Should this run without power */
	public boolean runPowerLess();

	/** Set if this should run powerless */
	public void setPowerLess(boolean bool);
}

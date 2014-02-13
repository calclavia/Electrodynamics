package resonantinduction.api.mechanical;

/**
 * Interface applied to the manipulator.
 * 
 * @author Calclavia
 */
public interface IManipulator
{
	/**
	 * Find items going into the manipulator and input them into an inventory behind this
	 * manipulator.
	 */
	public void inject();

	/** Injects items */
	public void enject();
}

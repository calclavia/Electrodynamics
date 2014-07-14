package resonantinduction.electrical.encoder.coding;

import net.minecraft.world.World;
import resonant.lib.type.Pair;
import universalelectricity.core.transform.vector.Vector3;

/**
 * Simple interface too say an object is programmable
 * 
 * @author DarkGuardsman
 */
public interface IProgrammableMachine
{
	/** Current program in use */
	public IProgram getCurrentProgram();

	/** Sets the current program */
	public void setCurrentProgram(IProgram program);

	/**
	 * Gets the machine location as a pair containing both the world and xyz vector.
	 * 
	 * @return try to avoid returning null as every object but a fake one has a location
	 */
	public Pair<World, Vector3> getLocation();
}

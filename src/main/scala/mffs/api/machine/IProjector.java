package mffs.api.machine;

import com.resonant.wrapper.core.api.tile.BlockFrequency;
import net.minecraft.inventory.IInventory;
import nova.core.util.transform.Vector3d;

import java.util.Set;

/**
 * Also extends IDisableable, IFortronFrequency
 * @author Calclavia
 */
public abstract interface IProjector extends IInventory, IFieldMatrix, BlockFrequency {
	/**
	 * Projects the force field.
	 */
	public void projectField();

	/**
	 * Destroys the force field.
	 */
	public void destroyField();

	/**
	 * @return The speed in which a force field is constructed.
	 */
	public int getProjectionSpeed();

	/**
	 * @return The amount of ticks this projector has existed in the world.
	 */
	public long getTicks();

	/**
	 * DO NOT modify this list. Read-only.
	 * @return The actual force field block coordinates in the world.
	 */
	public Set<Vector3d> getForceFields();

}
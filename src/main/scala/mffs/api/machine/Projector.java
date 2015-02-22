package mffs.api.machine;

import mffs.api.Frequency;
import net.minecraft.inventory.IInventory;
import nova.core.util.transform.Vector3d;

import java.util.Set;

/**
 * Also extends IDisableable, IFortronFrequency
 *
 * @author Calclavia
 */
public interface Projector extends IInventory, FieldMatrix, Frequency {
	/**
	 * Projects the force field.
	 */
	void projectField();

	/**
	 * Destroys the force field.
	 */
	void destroyField();

	/**
	 * @return The speed in which a force field is constructed.
	 */
	int getProjectionSpeed();

	/**
	 * @return The amount of ticks this projector has existed in the world.
	 */
	long getTicks();

	/**
	 * DO NOT modify this list. Read-only.
	 *
	 * @return The actual force field block coordinates in the world.
	 */
	Set<Vector3d> getForceFields();

}
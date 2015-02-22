package mffs.api.modules;

import mffs.api.machine.FieldMatrix;
import mffs.api.machine.IProjector;
import nova.core.entity.Entity;
import nova.core.util.transform.Vector3d;
import nova.core.util.transform.Vector3i;
import nova.core.world.World;

import java.util.Set;

/**
 * A module for any matrix based machines.
 */
public interface Module extends FortronCost {
	/**
	 * Called before the projector projects a field.
	 *
	 * @param projector
	 * @return True to stop projecting.
	 */
	boolean onProject(IProjector projector, Set<Vector3d> field);

	boolean onDestroy(IProjector projector, Set<Vector3d> field);

	/**
	 * Called right before the projector creates a force field block.
	 *
	 * @return 0 - Do nothing; 1 - Skip this block and continue; 2 - Cancel rest of projection;
	 */
	int onProject(IProjector projector, Vector3d position);

	/**
	 * Called when an entity collides with a force field block.
	 *
	 * @return False to stop the default process of entity collision.
	 */
	boolean onCollideWithForceField(World world, Vector3i position, Entity entity);

	/**
	 * Called in this module when it is being calculated by the projector. Called BEFORE
	 * transformation is applied to the field.
	 *
	 * @return False if to prevent this position from being added to the projection que.
	 */
	void onPreCalculate(FieldMatrix projector, Set<Vector3d> calculatedField);

	/**
	 * Called in this module when after being calculated by the projector.
	 *
	 * @return False if to prevent this position from being added to the projection que.
	 */
	void onPostCalculate(FieldMatrix projector, Set<Vector3d> fieldDefinition);

	/**
	 * @return Does this module require ticking from the force field projector?
	 */
	boolean requireTicks();

}

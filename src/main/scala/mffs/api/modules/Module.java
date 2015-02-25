package mffs.api.modules;

import com.resonant.core.structure.Structure;
import mffs.api.machine.FieldMatrix;
import mffs.api.machine.Projector;
import nova.core.entity.Entity;
import nova.core.util.transform.Vector3i;
import nova.core.world.World;

import java.util.Set;

/**
 * A module for any matrix based machines.
 */
public interface Module extends FortronCost {
	/**
	 * Called before the projector projects a field.
	 * @param projector
	 * @return True to stop projecting.
	 */
	default boolean onCreateField(Projector projector, Set<Vector3i> field) {
		return false;
	}

	default boolean onDestroyField(Projector projector, Set<Vector3i> field) {
		return false;
	}

	/**
	 * Called right before the projector creates a force field block.
	 * @return 0 - Do nothing; 1 - Skip this block and continue; 2 - Cancel rest of projection;
	 */
	default int onProject(Projector projector, Vector3i position) {
		return 0;
	}

	/**
	 * Called when an entity collides with a force field block.
	 * @return False to stop the default process of entity collision.
	 */
	default boolean onCollideWithForceField(World world, Vector3i position, Entity entity) {
		return true;
	}

	/**
	 * Called in this module when it is being calculated by the projector. Called BEFORE
	 * transformation is applied to the field.
	 * @return False if to prevent this position from being added to the projection que.
	 */
	default void onCalculateExterior(FieldMatrix projector, Structure structure) {

	}

	default void onCalculateInterior(FieldMatrix projector, Structure structure) {

	}

	/**
	 * @return Does this module require ticking from the force field projector?
	 */
	default boolean requireTicks() {
		return false;
	}

}

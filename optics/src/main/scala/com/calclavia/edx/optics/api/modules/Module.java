package com.calclavia.edx.optics.api.modules;

import com.calclavia.edx.optics.api.machine.FieldMatrix;
import com.calclavia.edx.optics.api.machine.Projector;
import com.resonant.core.structure.Structure;
import nova.core.block.Block;
import nova.core.entity.Entity;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

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
	default boolean onCreateField(Projector projector, Set<Vector3D> field) {
		return false;
	}

	default boolean onDestroyField(Projector projector, Set<Vector3D> field) {
		return false;
	}

	/**
	 * Called right before the projector creates a force field block.
	 * @return The ProjectState, an instruction for the projector.
	 */
	default ProjectState onProject(Projector projector, Vector3D position) {
		return ProjectState.pass;
	}

	/**
	 * Called when an entity collides withPriority a force field block.
	 * @param block Block being collided withPriority
	 * @param entity Entity colliding withPriority block
	 * @return False to stop the default process of entity collision.
	 */
	default boolean onFieldCollide(Block block, Entity entity) {
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

	public static enum ProjectState {
		//Does nothing
		pass,
		//Skips the current block
		skip,
		//Cancels the projection
		cancel
	}
}

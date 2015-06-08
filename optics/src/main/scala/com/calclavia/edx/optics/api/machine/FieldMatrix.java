package com.calclavia.edx.optics.api.machine;

import com.calclavia.edx.optics.api.modules.StructureProvider;
import nova.core.item.Item;
import nova.core.item.ItemFactory;
import nova.core.util.Direction;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Set;

public interface FieldMatrix extends IActivatable, IPermissionProvider {
	/**
	 * Gets the mode of the projector, mainly the shape and size of it.
	 */
	StructureProvider getShape();

	Item getShapeItem();

	/**
	 * Gets the slot IDs based on the direction given.
	 */
	int[] getDirectionSlots(Direction direction);

	/**
	 * Gets the unspecified, direction-unspecific module slots on the left side of the GUI.
	 */
	int[] getModuleSlots();

	/**
	 * @param module - The module instance.
	 * @param direction - The direction facing.
	 * @return Gets the amount of modules based on the side.
	 */
	int getSidedModuleCount(ItemFactory module, Direction... direction);

	int getModuleCount(ItemFactory module, int... slots);

	/**
	 * Transformation information functions. Returns CACHED information unless the cache is cleared.
	 * Note that these are all RELATIVE to the projector's position.
	 */
	Vector3D getTranslation();

	Vector3D getPositiveScale();

	Vector3D getNegativeScale();

	int getRotationYaw();

	int getRotationPitch();

	/**
	 * @return Gets all the absolute block coordinates that are occupying the force field. Note that this is a copy of the actual field set.
	 */
	Set<Vector3D> getCalculatedField();

	/**
	 * Gets the absolute interior points of the projector. This might cause lag so call sparingly.
	 * @return
	 */
	Set<Vector3D> getInteriorPoints();
}

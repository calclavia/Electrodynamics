package mffs.api.modules;

import mffs.api.machine.FieldMatrix;
import mffs.api.machine.Projector;
import nova.core.util.transform.Vector3d;

import java.util.Set;

public interface IProjectorMode extends FortronCost {
	/**
	 * Called when the force field projector calculates the shape of the module.
	 * @param projector - The Projector Object. Can cast to TileEntity.
	 * @return The blocks actually making up the force field. This array of blocks are
	 * NOT affected by rotation or translation, and is relative to the center of the projector.
	 */
	public Set<Vector3d> getExteriorPoints(FieldMatrix projector);

	/**
	 * @return Gets all interior points. Not translated or rotated.
	 */
	public Set<Vector3d> getInteriorPoints(FieldMatrix projector);

	/**
	 * @return Is this specific position inside of this force field?
	 */
	public boolean isInField(FieldMatrix projector, Vector3d position);

	/**
	 * Called to render an object in front of the projection.
	 */
	public void render(Projector projector, double x, double y, double z, float f, long ticks);
}

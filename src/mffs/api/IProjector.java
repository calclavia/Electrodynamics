package mffs.api;

import java.util.Set;

import net.minecraft.inventory.IInventory;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.implement.IRotatable;

/**
 * Also extends IDisableable, IFortronFrequency
 * 
 * @author Calclavia
 * 
 */
public abstract interface IProjector extends IInventory, IRotatable, IBiometricIdentifierLink, IFieldInteraction
{

	/**
	 * @return Is the projector active?
	 */
	public boolean isActive();

	/**
	 * Projects a force field.
	 */
	public void projectField();

	/**
	 * Destroys a force field.
	 */
	public void destroyField();

	/**
	 * * @return Gets all the blocks that are occupying the force field.
	 */
	public Set<Vector3> getCalculatedField();

	/**
	 * @return The speed in which a force field is constructed.
	 */
	public int getProjectionSpeed();

	/**
	 * * @return The amount of ticks this projector has existed in the world.
	 */
	public long getTicks();

}
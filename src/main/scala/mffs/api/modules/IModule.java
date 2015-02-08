package mffs.api.modules;

import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nova.core.util.transform.Vector3d;

import java.util.Set;

/**
 * A module for any matrix based machines.
 */
public interface IModule extends IFortronCost {
	/**
	 * Called before the projector projects a field.
	 * @param projector
	 * @return True to stop projecting.
	 */
	boolean onProject(IProjector projector, Set<Vector3d> field);

	boolean onDestroy(IProjector projector, Set<Vector3d> field);

	/**
	 * Called right before the projector creates a force field block.
	 * @return 0 - Do nothing; 1 - Skip this block and continue; 2 - Cancel rest of projection;
	 */
	public int onProject(IProjector projector, Vector3d position);

	/**
	 * Called when an entity collides with a force field block.
	 * @return False to stop the default process of entity collision.
	 */
	public boolean onCollideWithForceField(World world, int x, int y, int z, Entity entity, ItemStack moduleStack);

	/**
	 * Called in this module when it is being calculated by the projector. Called BEFORE
	 * transformation is applied to the field.
	 * @return False if to prevent this position from being added to the projection que.
	 */
	public void onPreCalculate(IFieldMatrix projector, Set<Vector3d> calculatedField);

	/**
	 * Called in this module when after being calculated by the projector.
	 * @return False if to prevent this position from being added to the projection que.
	 */
	public void onPostCalculate(IFieldMatrix projector, Set<Vector3d> fieldDefinition);

	/**
	 * @param moduleStack
	 * @return Does this module require ticking from the force field projector?
	 */
	public boolean requireTicks(ItemStack moduleStack);

}

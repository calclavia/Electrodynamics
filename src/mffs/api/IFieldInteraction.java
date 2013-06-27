package mffs.api;

import java.util.Set;

import mffs.api.modules.IModule;
import mffs.api.modules.IModuleAcceptor;
import mffs.api.modules.IProjectorMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

public interface IFieldInteraction extends IModuleAcceptor
{
	/**
	 * Gets the mode of the projector, mainly the shape and size of it.
	 */
	public IProjectorMode getMode();

	public ItemStack getModeStack();

	/**
	 * Gets the slot IDs based on the direction given.
	 */
	public int[] getSlotsBasedOnDirection(ForgeDirection direction);

	/**
	 * Gets the unspecified, direction-unspecific module slots on the left side of the GUI.
	 */
	public int[] getModuleSlots();

	/**
	 * @param module - The module instance.
	 * @param direction - The direction facing.
	 * @return Gets the amount of modules based on the side.
	 */
	public int getSidedModuleCount(IModule module, ForgeDirection... direction);

	/**
	 * Transformation information functions. Returns CACHED information unless the cache is cleared.
	 */
	public Vector3 getTranslation();

	public Vector3 getPositiveScale();

	public Vector3 getNegativeScale();

	public int getRotationYaw();

	public int getRotationPitch();

	/**
	 * Gets the interior points of the projector. This might cause lag so call sparingly.
	 * 
	 * @return
	 */
	public Set<Vector3> getInteriorPoints();

}

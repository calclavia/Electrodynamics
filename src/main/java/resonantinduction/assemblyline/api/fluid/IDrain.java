package resonantinduction.assemblyline.api.fluid;

import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.interfaces.IRotatable;

/** Interface to make or use the TileEntityDrain. This is mostly a dummy interface to help the
 * construction pump use the TileEntity as the center of drain location
 * 
 * The use of ITankContainer is optional but is need for the drain to be added to a Fluid Network
 * Same goes for IRotatable but make sure to return direction as the direction the drain faces */
public interface IDrain extends IFluidHandler, IRotatable
{
    /** Can the pump drain in the area in the given direction
     * 
     * @param direction - not the side of the block but rather the direction the block is facing
     * @return true if it can */
    public boolean canDrain(ForgeDirection direction);

    /** Can the pump fill in the area in the given direction
     * 
     * @param direction - not the side of the block but rather the direction the block is facing
     * @return true if it can */
    public boolean canFill(ForgeDirection direction);

    /** Gets the list of fillable blocks */
    public Set<Vector3> getFillList();

    /** Gets the list of drainable blocks */
    public Set<Vector3> getFluidList();

    /** Call this after the drain was used to edit a block at the location */
    public void onUse(Vector3 vec);

}

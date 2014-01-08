package resonantinduction.assemblyline.redstone;

import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.interfaces.IMultiBlock;
import com.builtbroken.minecraft.prefab.TileEntityMachine;

public class TileEntityPistonPlus extends TileEntityMachine implements IMultiBlock
{
    int extensionLimit = 1;
    boolean isExtended = false;

    @Override
    public Vector3[] getMultiBlockVectors()
    {
        // TODO Auto-generated method stub
        return null;
    }

}

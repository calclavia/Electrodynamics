package resonantinduction.api;

import net.minecraft.tileentity.TileEntity;
import universalelectricity.api.net.INetwork;

public interface IBeltNetwork extends INetwork<IBeltNetwork, IBelt, TileEntity>
{
    /** Frame of animation the belts all share */
    public int frame();

    /** Speed to apply to each entity */
    public float speed();

    public void reconstruct();
}

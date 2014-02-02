package resonantinduction.electrical.charger;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockRI;

/** Block that is used to charge an item on its surface
 * 
 * @author Darkguardsman */
public class BlockCharger extends BlockRI
{
    public BlockCharger()
    {
        super("BlockItemCharger");
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileCharger();
    }

}

package resonantinduction.atomic.machine.reactor;

import net.minecraft.block.material.Material;
import resonant.content.spatial.block.SpatialBlock;
import resonant.lib.prefab.vector.Cuboid;

/** Control rod block */
public class TileControlRod extends SpatialBlock
{
    public TileControlRod()
    {
        super(Material.iron);
        bounds = new Cuboid(0.3f, 0f, 0.3f, 0.7f, 1f, 0.7f);
        isOpaqueCube = false;
    }
}

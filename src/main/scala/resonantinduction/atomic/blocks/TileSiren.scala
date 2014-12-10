package resonantinduction.atomic.blocks

import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.prefab.tile.spatial.SpatialBlock
import resonantinduction.core.Reference
import resonant.lib.transform.vector.Vector3

/**
 * Siren block
 */
class TileSiren extends SpatialBlock(Material.wood)
{

    override def update
    {
        val world: World = worldObj
        if (world != null)
        {
            val metadata: Int = world.getBlockMetadata(xi, yi, zi)
            if (world.getBlockPowerInput(xi, yi, zi) > 0)
            {
                var volume: Float = 0.5f
                for (i <- 0 to 6)
                {
                    val check: Vector3 = toVector3.add(ForgeDirection.getOrientation(i))
                    if (check.getBlock(world) eq getBlockType)
                    {
                        volume *= 1.5f
                    }
                }
                world.playSoundEffect(x, y, z, Reference.prefix + "alarm", volume, 1f - 0.18f * (metadata / 15f))
            }
        }
    }

    override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
    {
        var metadata: Int = world.getBlockMetadata(xi, yi, zi)
        if (player.isSneaking)
        {
            metadata -= 1
        }
        else
        {
            metadata += 1
        }
        metadata = Math.max(metadata % 16, 0)
        world.setBlockMetadataWithNotify(xi, yi, zi, metadata, 2)
        return true
    }
}
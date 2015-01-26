package edx.quantum.reactor

import net.minecraft.block.material.Material
import resonantengine.lib.prefab.tile.spatial.ResonantBlock
import resonantengine.lib.transform.region.Cuboid

/**
 * Control rod block
 */
class TileControlRod extends ResonantBlock(Material.iron)
{
  bounds = new Cuboid(0.3f, 0f, 0.3f, 0.7f, 1f, 0.7f)
  isOpaqueCube = false
  normalRender = false
  renderStaticBlock = true
}
package edx.quantum.machine.fulmination

import net.minecraft.block.material.Material
import resonant.lib.grid.energy.EnergyStorage
import resonant.lib.prefab.tile.spatial.SpatialTile
import resonant.lib.prefab.tile.traits.TEnergyProvider

/**
 * Fulmination TileEntity
 */
object TileFulmination
{
  private final val maxEnergy: Long = 10000000000000L
}

class TileFulmination extends SpatialTile(Material.iron) with TEnergyProvider
{
  //TODO: Dummy
  energy = new EnergyStorage
  energy.max = TileFulmination.maxEnergy * 2
  blockHardness = 10
  blockResistance = 25000

  override def start
  {
    super.start
    FulminationHandler.INSTANCE.register(this)
  }

  override def update
  {
    super.update
    energy -= 10
  }

  override def invalidate
  {
    FulminationHandler.INSTANCE.unregister(this)
    super.start
  }
}
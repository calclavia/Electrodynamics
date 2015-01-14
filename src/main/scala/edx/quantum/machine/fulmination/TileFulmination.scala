package edx.quantum.machine.fulmination

import net.minecraft.block.material.Material
import resonant.lib.content.prefab.TEnergyStorage
import resonant.lib.grid.energy.EnergyStorage
import resonant.lib.prefab.tile.TileElectric

/**
 * Fulmination TileEntity
 */
object TileFulmination
{
  private final val maxEnergy: Long = 10000000000000L
}

class TileFulmination extends TileElectric(Material.iron) with TEnergyStorage
{
  //TODO: Dummy
  energy = new EnergyStorage(0)
  energy.setCapacity(TileFulmination.maxEnergy * 2)
  this.blockHardness(10)
  this.blockResistance(25000)

  override def start
  {
    super.start
    FulminationHandler.INSTANCE.register(this)
  }

  override def update
  {
    super.update
    energy.extractEnergy(10, true)
  }

  override def invalidate
  {
    FulminationHandler.INSTANCE.unregister(this)
    super.start
  }
}
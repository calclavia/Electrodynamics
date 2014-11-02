package resonantinduction.atomic.machine.fulmination

import net.minecraft.block.material.Material
import resonant.lib.content.prefab.TEnergyStorage
import resonant.lib.content.prefab.java.TileElectric

/**
 * Fulmination TileEntity
 */
object TileFulmination
{
  private final val DIAN: Long = 10000000000000L
}

class TileFulmination extends TileElectric(Material.iron) with TEnergyStorage
{
  //Constructor
  energy.setCapacity(TileFulmination.DIAN * 2)
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
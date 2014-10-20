package resonantinduction.electrical.laser.emitter

import net.minecraft.block.material.Material
import net.minecraft.util.MovingObjectPosition
import resonant.content.prefab.java.TileAdvanced
import resonantinduction.electrical.laser.{ILaserHandler, Laser}
import universalelectricity.core.transform.vector.Vector3

/**
 * @author Calclavia
 */
class TileLaserEmitter extends TileAdvanced(Material.iron) with ILaserHandler
{
  var energy = 0D

  override def onLaserHit(renderStart: Vector3, incidentDirection: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double) = false

  override def update()
  {
    if (isIndirectlyPowered)
    {
      energy += world.getStrongestIndirectPower(xi, yi, zi) * (Laser.maxEnergy / 15)
    }

    if (energy > 0)
    {
      Laser.spawn(worldObj, asVector3 + 0.5 + new Vector3(getDirection) * 0.51, asVector3 + new Vector3(getDirection) * 0.6 + 0.5, new Vector3(getDirection), energy)
      energy = 0;
    }
  }
}

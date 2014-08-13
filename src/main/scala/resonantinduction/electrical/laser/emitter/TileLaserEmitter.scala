package resonantinduction.electrical.laser.emitter

import net.minecraft.util.MovingObjectPosition
import universalelectricity.core.transform.vector.Vector3
import resonantinduction.electrical.laser.{ILaserHandler, Laser, TileBase}

/**
 * @author Calclavia
 */
class TileLaserEmitter extends TileBase with ILaserHandler
{
  var energy = 0D

  override def onLaserHit(renderStart: Vector3, incidentDirection: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double) = false

  override def updateEntity()
  {
    if (isPowered())
    {
      energy += world.getStrongestIndirectPower(x, y, z) * (Laser.maxEnergy / 15)
    }

    if (energy > 0)
    {
      Laser.spawn(worldObj, position + 0.5 + new Vector3(direction) * 0.51, position + new Vector3(direction) * 0.6 + 0.5, new Vector3(direction), energy)
      energy = 0;
    }
  }
}

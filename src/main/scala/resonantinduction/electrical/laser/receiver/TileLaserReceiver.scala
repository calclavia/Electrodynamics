package resonantinduction.electrical.laser.receiver

import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.electrical.laser.{ILaserHandler, Laser, TileBase}
import universalelectricity.core.transform.vector.Vector3

/**
 * @author Calclavia
 */
class TileLaserReceiver extends TileBase with ILaserHandler
{
  private var energy = 0D

  var redstoneValue = 0
  private var prevRedstoneValue = 0;

  override def update()
  {
    if (energy > 0)
    {
      redstoneValue = Math.min(Math.ceil(energy / (Laser.maxEnergy / 15)), 15).toInt

      if (redstoneValue != prevRedstoneValue)
      {
        world.notifyBlocksOfNeighborChange(xi, yi, zi, getBlockType)
        ForgeDirection.VALID_DIRECTIONS.foreach(dir => world.notifyBlocksOfNeighborChange(xi + dir.offsetX, yi + dir.offsetY, zi + dir.offsetZ, getBlockType))
        prevRedstoneValue = redstoneValue
      }

      energy = 0
    }
    else
    {
      redstoneValue = 0

      if (redstoneValue != prevRedstoneValue)
      {
        world.notifyBlocksOfNeighborChange(xi, yi, zi, getBlockType)
        ForgeDirection.VALID_DIRECTIONS.foreach(dir => world.notifyBlocksOfNeighborChange(xi + dir.offsetX, yi + dir.offsetY, zi + dir.offsetZ, getBlockType))
        prevRedstoneValue = redstoneValue
      }
    }
  }

  override def onLaserHit(renderStart: Vector3, incident: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double): Boolean =
  {
    if (hit.sideHit == direction.ordinal)
    {
      this.energy += energy
    }

    return false
  }
}

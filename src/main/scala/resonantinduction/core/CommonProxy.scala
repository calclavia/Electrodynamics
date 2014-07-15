/**
 * @Calclavia
 */
package resonantinduction.core

import java.awt._

import net.minecraft.world.World
import resonant.lib.prefab.AbstractProxy
import universalelectricity.core.transform.vector.Vector3

/**
 * @author Calclavia
 */
class CommonProxy extends AbstractProxy
{
  def isPaused: Boolean =
  {
    return false
  }

  def isGraphicsFancy: Boolean =
  {
    return false
  }

  def renderBlockParticle(world: World, x: Double, y: Double, z: Double, velocity: Vector3, blockID: Int, scale: Float)
  {
  }

  def renderBlockParticle(world: World, position: Vector3, velocity: Vector3, blockID: Int, scale: Float)
  {
  }

  def renderBeam(world: World, position: Vector3, hit: Vector3, color: Color, age: Int)
  {
  }

  def renderBeam(world: World, position: Vector3, target: Vector3, red: Float, green: Float, blue: Float, age: Int)
  {
  }
}
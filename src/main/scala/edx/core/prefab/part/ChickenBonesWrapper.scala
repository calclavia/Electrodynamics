package edx.core.prefab.part

import codechicken.lib.vec.Cuboid6
import resonantengine.lib.transform.region.Cuboid

/**
 * Wraps ChickenBone's Vector3 with UE's Vector3.
 * @author Calclavia
 */
object ChickenBonesWrapper
{
  implicit def asUEVector3(vec: codechicken.lib.vec.Vector3): resonantengine.lib.transform.vector.Vector3 = new resonantengine.lib.transform.vector.Vector3(vec.x, vec.y, vec.z)

  implicit def asUEVector3(vec: codechicken.lib.vec.BlockCoord): resonantengine.lib.transform.vector.Vector3 = new resonantengine.lib.transform.vector.Vector3(vec.x, vec.y, vec.z)

  implicit def asCBVector3(vec: resonantengine.lib.transform.vector.Vector3): codechicken.lib.vec.Vector3 = new codechicken.lib.vec.Vector3(vec.x, vec.y, vec.z)

  implicit def asCuboid(cuboid: Cuboid6): Cuboid = new Cuboid(cuboid.min, cuboid.max)

  implicit def asCuboid6(cuboid: Cuboid): Cuboid6 = new Cuboid6(cuboid.min, cuboid.max)
}

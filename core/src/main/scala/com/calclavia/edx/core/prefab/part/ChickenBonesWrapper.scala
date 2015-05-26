package com.calclavia.edx.core.prefab.part

import codechicken.lib.vec.Cuboid6
import resonantengine.lib.transform.region.Cuboid

/**
 * Wraps ChickenBone's Vector3d with UE's Vector3d.
 * @author Calclavia
 */
object ChickenBonesWrapper
{
  implicit def asUEVector3(vec: codechicken.lib.vec.Vector3d): resonantengine.lib.transform.vector.Vector3d = new resonantengine.lib.transform.vector.Vector3d(vec.x, vec.y, vec.z)

  implicit def asUEVector3(vec: codechicken.lib.vec.BlockCoord): resonantengine.lib.transform.vector.Vector3d = new resonantengine.lib.transform.vector.Vector3d(vec.x, vec.y, vec.z)

  implicit def asCBVector3(vec: resonantengine.lib.transform.vector.Vector3d): codechicken.lib.vec.Vector3d = new codechicken.lib.vec.Vector3d(vec.x, vec.y, vec.z)

  implicit def asCuboid(cuboid: Cuboid6): Cuboid = new Cuboid(cuboid.min, cuboid.max)

  implicit def asCuboid6(cuboid: Cuboid): Cuboid6 = new Cuboid6(cuboid.min, cuboid.max)
}

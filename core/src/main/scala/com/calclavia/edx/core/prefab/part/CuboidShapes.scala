package com.calclavia.edx.core.prefab.part

import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.vec.{Cuboid6, Rotation, Vector3}

/**
 * Reference sheet for commonly created cuboid shape sets.
 * Created by robert on 10/18/2014.
 */
object CuboidShapes
{
  /** 0.3 box shaped centered wire */
  lazy val segment = getNewSegments(0.375f)
  /** 0.4 box shaped wire designed to be used for insulation */
  lazy val thickSegment = getNewSegments(0.3f)
  /** Center segment of wire */
  lazy val center = new IndexedCuboid6(7, new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625))
  /** Center segment of insulation */
  lazy val thickCenter = new IndexedCuboid6(7, new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7))
  /** 1/8th thick panel box can be used for anything flat */
  lazy val panel = getNewPanelSegments

  /**
   * Generates then returns a new set of panel segments that are .125m in size
   * @return 6 part matrix
   */
  def getNewPanelSegments: Array[Array[Cuboid6]] =
  {
    val segments = Array.ofDim[Cuboid6](6, 2)
    segments(0)(0) = new Cuboid6(1 / 8D, 0, 0, 7 / 8D, 1 / 8D, 1)
    segments(0)(1) = new Cuboid6(0, 0, 1 / 8D, 1, 1 / 8D, 7 / 8D)

    for (s <- 1 until 6)
    {
      val t = Rotation.sideRotations(s).at(Vector3.center)
      segments(s)(0) = segments(0)(0).copy().apply(t)
      segments(s)(1) = segments(0)(1).copy().apply(t)
    }
    return segments
  }

  /**
   * Generates then returns a new set of wire segments that are .3m in size
   * @return 7 part array
   */
  def getNewSegments(thickness: Float): Array[IndexedCuboid6] =
  {
    val segments = new Array[IndexedCuboid6](7)
    segments(0) = new IndexedCuboid6(0, new Cuboid6(thickness, 0.0, thickness, 1 - thickness, thickness, 1 - thickness))
    segments(1) = new IndexedCuboid6(1, new Cuboid6(thickness, 1 - thickness, thickness, 1 - thickness, 1.0, 1 - thickness))
    segments(2) = new IndexedCuboid6(2, new Cuboid6(thickness, thickness, 0.0, 1 - thickness, 1 - thickness, thickness))
    segments(3) = new IndexedCuboid6(3, new Cuboid6(thickness, thickness, 1 - thickness, 1 - thickness, 1 - thickness, 1.0))
    segments(4) = new IndexedCuboid6(4, new Cuboid6(0.0, thickness, thickness, thickness, 1 - thickness, 1 - thickness))
    segments(5) = new IndexedCuboid6(5, new Cuboid6(1 - thickness, thickness, thickness, 1.0, 1 - thickness, 1 - thickness))
    segments(6) = new IndexedCuboid6(6, new Cuboid6(thickness, thickness, thickness, 1 - thickness, 1 - thickness, 1 - thickness))
    return segments
  }
}

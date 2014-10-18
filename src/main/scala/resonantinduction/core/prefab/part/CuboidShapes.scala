package resonantinduction.core.prefab.part

import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.vec.Cuboid6

/** Reference sheet for commonly created cuboid shape sets.
 * Created by robert on 10/18/2014.
 */
final object CuboidShapes
{
    lazy val WIRE_SEGMENTS = getNewWireSegments()
    lazy val WIRE_INSULATION = getNewWireInsulationSegments()

    lazy val WIRE_CENTER: IndexedCuboid6 = new IndexedCuboid6(7, new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625))

    /**
     * Generates then returns a new set of wire segments that are .3 in size
     * @return 7 part array
     */
    def getNewWireSegments() : Array[IndexedCuboid6] =
    {
        val segments = new Array[IndexedCuboid6](7)
        segments(0) = new IndexedCuboid6(0, new Cuboid6(0.36, 0.000, 0.36, 0.64, 0.36, 0.64))
        segments(1) = new IndexedCuboid6(1, new Cuboid6(0.36, 0.64, 0.36, 0.64, 1.000, 0.64))
        segments(2) = new IndexedCuboid6(2, new Cuboid6(0.36, 0.36, 0.000, 0.64, 0.64, 0.36))
        segments(3) = new IndexedCuboid6(3, new Cuboid6(0.36, 0.36, 0.64, 0.64, 0.64, 1.000))
        segments(4) = new IndexedCuboid6(4, new Cuboid6(0.000, 0.36, 0.36, 0.36, 0.64, 0.64))
        segments(5) = new IndexedCuboid6(5, new Cuboid6(0.64, 0.36, 0.36, 1.000, 0.64, 0.64))
        segments(6) = new IndexedCuboid6(6, new Cuboid6(0.36, 0.36, 0.36, 0.64, 0.64, 0.64))
        return segments
    }

    /**
     * Generates then returns a new set of insulation segments that are .4 in size
     * @return 7 part array
     */
    def getNewWireInsulationSegments() : Array[IndexedCuboid6] =
    {
        val segments = new Array[IndexedCuboid6](7)
        segments(0) = new IndexedCuboid6(0, new Cuboid6(0.3, 0.0, 0.3, 0.7, 0.3, 0.7))
        segments(1) = new IndexedCuboid6(1, new Cuboid6(0.3, 0.7, 0.3, 0.7, 1.0, 0.7))
        segments(2) = new IndexedCuboid6(2, new Cuboid6(0.3, 0.3, 0.0, 0.7, 0.7, 0.3))
        segments(3) = new IndexedCuboid6(3, new Cuboid6(0.3, 0.3, 0.7, 0.7, 0.7, 1.0))
        segments(4) = new IndexedCuboid6(4, new Cuboid6(0.0, 0.3, 0.3, 0.3, 0.7, 0.7))
        segments(5) = new IndexedCuboid6(5, new Cuboid6(0.7, 0.3, 0.3, 1.0, 0.7, 0.7))
        segments(6) = new IndexedCuboid6(6, new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7))
        return segments
    }
}

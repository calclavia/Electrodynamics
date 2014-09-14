package resonantinduction.mechanical.fluid.pipe

/**
 * Enumerator to hold info about each pipe material.
 *
 * @author Calclavia
 */
object PipeMaterials extends Enumeration
{
  case class PipeMaterial(maxPressure: Int, maxFlow: Int, color: Int) extends Val

  val ceramic = PipeMaterial(5, 5, 0xB3866F)
  val bronze = PipeMaterial(25, 25, 0xD49568)
  val plastic = PipeMaterial(50, 30, 0xDAF4F7)
  val iron = PipeMaterial(100, 50, 0x5C6362)
  val steel = PipeMaterial(100, 100, 0x888888)
  val fiberglass = PipeMaterial(1000, 200, 0x9F9691)
}


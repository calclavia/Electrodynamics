package resonantinduction.mechanical.fluid.pipe

/**
 * Enumerator to hold info about each pipe material.
 *
 * @author Calclavia
 */
object PipeMaterial extends Enumeration
{
  case class PipeMaterial(maxPressure: Int, maxFlow: Int, color: Int) extends Val

  val CERAMIC = PipeMaterial(5, 5, 0xB3866F)
  val BRONZE = PipeMaterial(25, 25, 0xD49568)
  val PLASTIC = PipeMaterial(50, 30, 0xDAF4F7)
  val IRON = PipeMaterial(100, 50, 0x5C6362)
  val STEEL = PipeMaterial(100, 100, 0x888888)
  val FIBERGLASS = PipeMaterial(1000, 200, 0x9F9691)
}


package resonantinduction.core.resource.content

import net.minecraftforge.fluids.Fluid

class FluidColored(fluidName: String) extends Fluid(fluidName)
{
  var color = 0xFFFFFF

  def setColor(color: Int): FluidColored =
  {
    this.color = color
    return this
  }

  override def getColor = color
}
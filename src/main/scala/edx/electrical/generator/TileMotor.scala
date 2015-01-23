package edx.electrical.generator

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import edx.mechanical.mech.grid.NodeMechanical
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{ChatComponentText, ResourceLocation}
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonant.lib.content.prefab.TIO
import resonant.lib.grid.core.TSpatialNodeProvider
import resonant.lib.prefab.tile.spatial.SpatialTile
import resonant.lib.prefab.tile.traits.{TElectric, TRotatable}
import resonant.lib.render.RenderUtility
import resonant.lib.transform.vector.Vector3

/**
 * A kinetic energy to electrical energy converter.
 *
 * @author Calclavia
 */
object TileMotor
{
  @SideOnly(Side.CLIENT)
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "motor.tcn"))
  @SideOnly(Side.CLIENT)
  val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "motor.png")

  val fieldStrength = 1
  val coils = 10
  val area = 1

  /**
   * Motor constant is the product of: N (Number of coils), B (Magnetic Field Density), A (Area)
   * Or, we can call it: N * Total Flux
   */
  val motorConstant = fieldStrength * area * coils
}

class TileMotor extends SpatialTile(Material.iron) with TIO with TElectric with TSpatialNodeProvider with TRotatable
{
  var mechNode = new NodeMechanical(this)
  {
    override def canConnect(from: ForgeDirection): Boolean =
    {
      connectionMask = 1 << getDirection.getOpposite.ordinal
      return super.canConnect(from)
    }
  }

  private var gearRatio = 0

  textureName = "material_wood_surface"
  normalRender = false
  isOpaqueCube = false
  ioMap = 0
  saveIOMap = true
  nodes.add(electricNode)
  nodes.add(mechNode)

  electricNode.resistance = 10

  def toggleGearRatio() = (gearRatio + 1) % 3

  override def start()
  {
    super.start()
    updateConnections()
  }

  def updateConnections()
  {
    electricNode.connectionMask = ForgeDirection.VALID_DIRECTIONS.filter(getIO(_) > 0).map(d => 1 << d.ordinal()).foldLeft(0)(_ | _)
    electricNode.positiveTerminals.clear()
    electricNode.negativeTerminals.clear()
    electricNode.positiveTerminals.addAll(getInputDirections())
    electricNode.negativeTerminals.addAll(getOutputDirections())
    electricNode.reconstruct()
    notifyChange()
    markUpdate()
  }

  override def update()
  {
    super.update()

    /**
     * Motors produce emf or counter-emf by Lenz's law based on angular velocity
     * emf = change of flux/time
     *
     * After differentiation via chain rule:
     * emf = NBAwSin(wt)
     * emfMax = NBAw
     * where w = angular velocity
     */
    val inducedEmf = TileMotor.motorConstant * mechNode.angularVelocity // * Math.sin(mechNode.angularVelocity * System.currentTimeMillis() / 1000d)

    /**
     * Produce torque based on current.
     * T = NBA * I / (2pi)
     */
    //TODO: Torque should be based on the current, as a result of counter-emf
    val torque = TileMotor.motorConstant * ((electricNode.voltage - inducedEmf) / electricNode.resistance) / (2 * Math.PI)
    mechNode.accelerate(torque)
    //    electricNode.generateVoltage(inducedEmf * -1)
  }

  override def setIO(dir: ForgeDirection, ioType: Int)
  {
    if (dir != getDirection || dir != getDirection.getOpposite)
    {
      super.setIO(dir, ioType)

      //Auto-set opposite side for unreachable sides
      if (ioType != 0)
        super.setIO(dir.getOpposite, (ioType % 2) + 1)
      updateConnections()
    }
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int): Unit =
  {
    GL11.glPushMatrix()
    GL11.glTranslatef(pos.x.toFloat + 0.5f, pos.y.toFloat + 0.5f, pos.z.toFloat + 0.5f)
    GL11.glRotatef(90, 0, 1, 0)
    RenderUtility.rotateBlockBasedOnDirection(getDirection)
    RenderUtility.bind(TileMotor.texture)
    TileMotor.model.renderAll()
    GL11.glPopMatrix()
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super[TIO].readFromNBT(nbt)
    super.readFromNBT(nbt)
    gearRatio = nbt.getByte("gear")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super[TIO].writeToNBT(nbt)
    super.writeToNBT(nbt)
    nbt.setByte("gear", gearRatio.toByte)
  }

  override def toString: String = "[TileMotor]" + x + "x " + y + "y " + z + "z "

  override protected def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.isSneaking)
    {
      if (!world.isRemote)
      {
        gearRatio = (gearRatio + 1) % 3
        player.addChatComponentMessage(new ChatComponentText("Toggled gear ratio: " + gearRatio))
      }
      return true
    }

    return super.configure(player, side, hit)
  }

}
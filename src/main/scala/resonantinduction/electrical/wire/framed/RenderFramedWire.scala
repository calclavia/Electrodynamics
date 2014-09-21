package resonantinduction.electrical.wire.framed

import java.nio.FloatBuffer
import java.util.Map

import codechicken.lib.render.uv.IconTransformation
import codechicken.lib.render.{CCModel, CCRenderState, ColourMultiplier, TextureUtils}
import codechicken.lib.vec.{Rotation, Translation}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.wrapper.BitmaskWrapper._
import resonantinduction.core.util.ResonantUtil

/**
 * Renderer for framed wires
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
object RenderFramedWire
{
  val models: Map[String, CCModel] = null
  var wireIIcon: IIcon = null
  var insulationIIcon: IIcon = null
  var breakIIcon: IIcon = null

  def loadBuffer(buffer: FloatBuffer, src: Float*)
  {
    buffer.clear
    buffer.put(src.toArray)
    buffer.flip
  }

  def renderStatic(wire: PartFramedWire)
  {
    TextureUtils.bindAtlas(0)
    CCRenderState.reset()
    CCRenderState.useColour = true
    CCRenderState.setBrightness(wire.world, wire.x, wire.y, wire.z)
    renderSide(ForgeDirection.UNKNOWN, wire)

    ForgeDirection.VALID_DIRECTIONS.filter(s => wire.connectionMask.mask(s)).foreach(renderSide(_, wire))
  }

  def renderSide(side: ForgeDirection, wire: PartFramedWire)
  {
    var name: String = side.name.toLowerCase
    name = if (name == "unknown") "center" else name

    renderPart(wireIIcon, models.get(name), wire.x, wire.y, wire.z, wire.material.color)

    if (wire.insulated)
      renderPart(insulationIIcon, models.get(name + "Insulation"), wire.x, wire.y, wire.z, ResonantUtil.getColorHex(wire.getColor))
  }

  def renderPart(icon: IIcon, cc: CCModel, x: Double, y: Double, z: Double, color: Int)
  {
    cc.render(0, cc.verts.length, Rotation.sideOrientation(0, Rotation.rotationTo(0, 2)).at(codechicken.lib.vec.Vector3.center).`with`(new Translation(x, y, z)), new IconTransformation(icon), new ColourMultiplier(color))
  }
}
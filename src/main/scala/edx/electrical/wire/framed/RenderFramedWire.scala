package edx.electrical.wire.framed

import codechicken.lib.lighting.LightModel
import codechicken.lib.render.uv.IconTransformation
import codechicken.lib.render.{CCModel, CCRenderState, ColourMultiplier, TextureUtils}
import codechicken.lib.vec.{Rotation, Translation}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import edx.core.render.InvertX
import edx.core.util.ResonantUtil
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.lib.wrapper.BitmaskWrapper._

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * Renderer for framed wires
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
object RenderFramedWire
{
  var models = mutable.Map.empty[String, CCModel]
  var wireIcon: IIcon = null
  var insulationIcon: IIcon = null
  var breakIIcon: IIcon = null

  models = CCModel.parseObjModels(new ResourceLocation(Reference.domain, "models/wire.obj"), 7, new InvertX())
  models.values.foreach(c =>
  {
    c.apply(new Translation(.5, 0, .5))
    c.computeLighting(LightModel.standardLightModel)
    c.shrinkUVs(0.0005)
  })

  def renderStatic(wire: PartFramedWire)
  {
    TextureUtils.bindAtlas(0)
    CCRenderState.reset()
    CCRenderState.useColour = true
    CCRenderState.setBrightness(wire.world, wire.x, wire.y, wire.z)
    renderSide(ForgeDirection.UNKNOWN, wire)

    ForgeDirection.VALID_DIRECTIONS.filter(s => wire.clientRenderMask.mask(s)).foreach(renderSide(_, wire))
  }

  def renderSide(side: ForgeDirection, wire: PartFramedWire)
  {
    var name: String = side.name.toLowerCase
    name = if (name == "unknown") "center" else name

    renderPart(wireIcon, models(name), wire.x, wire.y, wire.z, ResonantUtil.convertRGBtoRGBA(wire.material.color))

    if (wire.insulated)
      renderPart(insulationIcon, models(name + "Insulation"), wire.x, wire.y, wire.z, ResonantUtil.convertRGBtoRGBA(ResonantUtil.getColorHex(wire.getColor)))
  }

  def renderPart(icon: IIcon, cc: CCModel, x: Double, y: Double, z: Double, color: Int)
  {
    val transform = Rotation.sideOrientation(0, Rotation.rotationTo(0, 2)).at(codechicken.lib.vec.Vector3.center).`with`(new Translation(x, y, z))
    cc.render(0, cc.verts.length, transform, new IconTransformation(icon), new ColourMultiplier(color))
  }
}
package resonantinduction.electrical.wire.flat

import java.util.{Arrays, LinkedList}

import codechicken.lib.lighting.LightModel
import codechicken.lib.math.MathHelper
import codechicken.lib.render._
import codechicken.lib.render.uv._
import codechicken.lib.vec.{Cuboid6, Rotation, Transformation, Translation, Vector3}
import net.minecraft.util.IIcon
import resonantinduction.core.util.ResonantUtil

object RenderFlatWire
{
  var flatWireTexture: IIcon = null
  var reorientSide: Array[Int] = Array[Int](0, 3, 3, 0, 0, 3)
  /**
   * Array of all built models. These will be generated on demand.
   */
  var wireModels: Array[CCModel] = new Array[CCModel](3 * 6 * 256)
  var invModels: Array[CCModel] = new Array[CCModel](3)
  val gen_inst = new WireModelGenerator

  /**
   * Puts verts into model m starting at index k
   */
  def addVerts(m: CCModel, verts: Array[Vertex5], k: Int): Int =
  {
    {
      var i: Int = 0
      while (i < verts.length)
      {
        m.verts(k + i) = verts(i)
        ({i += 1; i - 1 })
      }
    }
    return k + verts.length
  }

  def finishModel(m: CCModel): CCModel =
  {
    m.apply(new UVScale(1 / 32D))
    m.shrinkUVs(0.0005)
    m.computeNormals
    m.computeLighting(LightModel.standardLightModel)
    return m
  }

  /**
   * Returns a tightly packed unique index for the specific model represented
   * by this wire. The mask is split into 3 sections the combination of
   * corresponding bits from the two lowest nybbles gives the connection type
   * in that direction.
   * 00 = none
   * 01 = corner
   * 10 = straight
   * 11 = internal The
   * second byte contains the thickness*6+side
   *
   * @param side The side the wire is attached to
   * @param thickness The thickness of the wire -1 in 1/8th blocks. Supported
   *                  values 0, 1, 2
   * @param connMap The connection mask of the wire
   */
  def modelKey(side: Int, thickness: Int, connMap: Int): Int =
  {
    var key: Int = connMap & 0xFF
    val renderCorner: Int = connMap >> 20 & 0xF
    key |= (renderCorner ^ key & 0xF) << 4
    key &= ~0xF | renderCorner
    val internal: Int = (connMap & 0xF00) >> 8
    key |= internal << 4 | internal
    key |= side + thickness * 6 << 8
    return key
  }

  def modelKey(w: PartFlatWire): Int =
  {
    return modelKey(w.side, w.getThickness, w.connMap)
  }

  def getOrGenerateModel(key: Int): CCModel =
  {
    var m: CCModel = wireModels(key)
    if (m == null) wireModels(key) =
      {m = gen_inst.generateModel(key, false); m }
    return m
  }

  def render(wire: PartFlatWire, pos: Vector3)
  {
    val colorCode = ResonantUtil.getColorHex(wire.getColor)
    val operation = if (colorCode == -1) ColourMultiplier.instance(0xFFFFFF) else new ColourMultiplier(colorCode)
    val model = getOrGenerateModel(modelKey(wire))
    model.render(new Translation(pos), new IconTransformation(wire.getIcon), operation)
  }

  def renderInv(thickness: Int, t: Transformation, icon: IIcon)
  {
    var m: CCModel = invModels(thickness)
    if (m == null) invModels(thickness) =
      {m = gen_inst.generateInvModel(thickness); m }
    m.render(t, new IconTransformation(icon))
  }

  def renderBreakingOverlay(icon: IIcon, wire: PartFlatWire)
  {
    val key: Int = modelKey(wire)
    val side: Int = (key >> 8) % 6
    val w: Double = ((key >> 8) / 6 + 1) / 16D
    val h: Double = w + 1 / 16D
    val mask: Int = key & 0xFF
    val connMask: Int = (mask & 0xF0) >> 4 | mask & 0xF
    val connCount: Int = WireModelGenerator.countConnections(connMask)
    val boxes: LinkedList[Cuboid6] = new LinkedList[Cuboid6]
    boxes.add(new Cuboid6(0.5 - w, 0, 0.5 - w, 0.5 + w, h, 0.5 + w).apply(Rotation.sideRotations(side).at(Vector3.center)))

    for (r <- (0 until 4))
    {
      var length: Int = 0
      if (connCount == 0)
      {
        if (r % 2 == 1) length = 4
        else length = 0
      }
      else if (connCount == 1)
      {
        if (connMask == (1 << (r + 2) % 4)) length = 4
        else if (connMask == (1 << r)) length = 8
        else length = 0
      }
      else length = if ((connMask & 1 << r) != 0) 8 else 0
      if (length > 0)
      {
        val l: Double = length / 16D
        boxes.add(new Cuboid6(0.5 - w, 0, 0.5 + w, 0.5 + w, h, 0.5 + l).apply(Rotation.sideOrientation(side, r).at(Vector3.center)))
      }
    }
  }


  class UVT(t: Transformation) extends UVTransformation
  {
    val vec = new Vector3

    def apply(uv: UV)
    {
      vec.set(uv.u, 0, uv.v).apply(t)
      uv.set(vec.x, vec.z)
    }

    def inverse: UVTransformation =
    {
      return null
    }

  }
}
package resonantinduction.electrical.wire.flat

import java.util.Arrays

import codechicken.lib.math.MathHelper
import codechicken.lib.render.uv.{UVTransformation, UVTranslation}
import codechicken.lib.render.{Vertex5, CCModel}
import codechicken.lib.vec.{Transformation, Vector3, Rotation}

/**
 * @author Calclavia
 */
object WireModelGenerator
{
  val sideReflect = new RenderFlatWire.UVT(Rotation.quarterRotations(2).at(new Vector3(8, 0, 16)))

  def countConnections(connMask: Int): Int =
  {
    var n: Int = 0

    for (r <- 0 until 4)
    {
      if ((connMask & 1 << r) != 0) n += 1
    }

    return n
  }
}

class WireModelGenerator
{
  private[flat] var side: Int = 0
  private[flat] var tw: Int = 0
  private[flat] var th: Int = 0
  private[flat] var w: Double = .0
  private[flat] var h: Double = .0
  private[flat] var mask: Int = 0
  private[flat] var connMask: Int = 0
  private[flat] var connCount: Int = 0
  private[flat] var model: CCModel = null
  private[flat] var i: Int = 0
  private[flat] var inv: Boolean = false

  def numFaces: Int =
  {
    if (inv) return 22
    var conns: Int = 0
    if (connCount <= 2) conns = 2
    else conns = connCount
    var faces: Int = conns * 3 + 5

    for (i <- 0 until 4)
    {
      if ((mask >> i & 0x11) == 1)
        faces += 1
    }

    return faces
  }

  def generateInvModel(thickness: Int): CCModel =
  {
    return generateModel(RenderFlatWire.modelKey(0, thickness, 0xF0), true)
  }

  def generateModel(key: Int, inv: Boolean): CCModel =
  {
    this.inv = inv
    side = (key >> 8) % 6
    tw = (key >> 8) / 6 + 1
    w = tw / 16D
    th = tw + 1
    h = th / 16D
    mask = key & 0xFF
    connMask = (mask & 0xF0) >> 4 | mask & 0xF
    connCount = WireModelGenerator.countConnections(connMask)
    model = CCModel.quadModel(numFaces * 4)
    i = 0
    generateCenter

    var r: Int = 0
    while (r < 4)
    {
      generateSide(r)
      ({r += 1; r - 1 })
    }

    model.apply(Rotation.sideOrientation(side, 0).at(Vector3.center))
    return RenderFlatWire.finishModel(model)
  }

  private def generateSide(r: Int)
  {
    val `type`: Int = mask >> r & 0x11
    var verts: Array[Vertex5] = null
    if (inv) verts = generateSideInv(r)
    else if (connCount == 0) if (r % 2 == 1) verts = generateStub(r)
    else verts = generateFlat(r)
    else if (connCount == 1) if (connMask == (1 << (r + 2) % 4)) verts = generateStub(r)
    else verts = generateSideFromType(`type`, r)
    else verts = generateSideFromType(`type`, r)
    val t: Transformation = Rotation.quarterRotations(r).at(Vector3.center)
    for (vert <- verts) vert.apply(t)
    i = RenderFlatWire.addVerts(model, verts, i)
  }

  private def generateSideInv(r: Int): Array[Vertex5] =
  {
    return withBottom(generateStraight(r), 4, 4)
  }

  private def generateSideFromType(`type`: Int, r: Int): Array[Vertex5] =
  {
    if (`type` == 0x00) return generateFlat(r)
    else if (`type` == 0x01) return generateCorner(r)
    else if (`type` == 0x10) return generateStraight(r)
    else return generateInternal(r)
  }

  private def generateFlat(r: Int): Array[Vertex5] =
  {
    val verts: Array[Vertex5] = Array[Vertex5](new Vertex5(0.5 - w, 0, 0.5 + w, 16, 16 + tw), new Vertex5(0.5 + w, 0, 0.5 + w, 16, 16 - tw), new Vertex5(0.5 + w, h, 0.5 + w, 16 - th, 16 - tw), new Vertex5(0.5 - w, h, 0.5 + w, 16 - th, 16 + tw))
    if (Rotation.rotateSide(side, r) % 2 == 0)
    {
      val uvt: RenderFlatWire.UVT = new RenderFlatWire.UVT(Rotation.quarterRotations(2).at(new Vector3(8, 0, 16)))
      for (vert <- verts) vert.apply(uvt)
    }
    return verts
  }

  private def generateStub(r: Int): Array[Vertex5] =
  {
    val verts: Array[Vertex5] = generateExtension(4)

    for (i <- 0 until 4)
    {
      verts(i).vec.z -= 0.002
    }

    reflectSide(verts, r)
    return verts
  }

  private def generateStraight(r: Int): Array[Vertex5] =
  {
    val verts: Array[Vertex5] = generateExtension(8)
    reflectSide(verts, r)
    return verts
  }

  private def generateCorner(r: Int): Array[Vertex5] =
  {
    var verts = generateExtension(8 + th)

    for (i <- 0 until 4)
    {
      verts(i).apply(new UVTranslation(0, -th))
    }

    verts = Arrays.copyOf(verts, 20)
    verts(16) = new Vertex5(0.5 - w, 0, 1, 8 - tw, 24 + 2 * th)
    verts(17) = new Vertex5(0.5 + w, 0, 1, 8 + tw, 24 + 2 * th)
    verts(18) = new Vertex5(0.5 + w, 0, 1 + h, 8 + tw, 24 + th)
    verts(19) = new Vertex5(0.5 - w, 0, 1 + h, 8 - tw, 24 + th)
    reflectSide(verts, r)
    return verts
  }

  private def generateInternal(r: Int): Array[Vertex5] =
  {
    val verts: Array[Vertex5] = generateExtension(8)
    verts(0).uv.set(8 + tw, 24)
    verts(1).uv.set(8 - tw, 24)
    verts(2).uv.set(8 - tw, 24 + tw)
    verts(3).uv.set(8 + tw, 24 + tw)
    reflectSide(verts, r)

    for (i <- 4 until 16)
    {
      verts(i).apply(new UVTranslation(16, 0))
    }

    return verts
  }

  private def generateExtension(tl: Int): Array[Vertex5] =
  {
    val l: Double = tl / 16D
    return Array[Vertex5](new Vertex5(0.5 - w, 0, 0.5 + l, 8 - tw, 24 + 2 * th), new Vertex5(0.5 + w, 0, 0.5 + l, 8 + tw, 24 + 2 * th), new Vertex5(0.5 + w, h, 0.5 + l, 8 + tw, 24 + th), new Vertex5(0.5 - w, h, 0.5 + l, 8 - tw, 24 + th), new Vertex5(0.5 - w, h, 0.5 + l, 8 - tw, 16 + tl), new Vertex5(0.5 + w, h, 0.5 + l, 8 + tw, 16 + tl), new Vertex5(0.5 + w, h, 0.5 + w, 8 + tw, 16 + tw), new Vertex5(0.5 - w, h, 0.5 + w, 8 - tw, 16 + tw), new Vertex5(0.5 - w, 0, 0.5 + w, 0, 16 + tw), new Vertex5(0.5 - w, 0, 0.5 + l, 0, 16 + tl), new Vertex5(0.5 - w, h, 0.5 + l, th, 16 + tl), new Vertex5(0.5 - w, h, 0.5 + w, th, 16 + tw), new Vertex5(0.5 + w, 0, 0.5 + l, 16, 16 + tl), new Vertex5(0.5 + w, 0, 0.5 + w, 16, 16 + tw), new Vertex5(0.5 + w, h, 0.5 + w, 16 - th, 16 + tw), new Vertex5(0.5 + w, h, 0.5 + l, 16 - th, 16 + tl))
  }

  private def generateCenter
  {
    var tex: Int = 0
    if (connCount == 0) tex = 1
    else if (connCount == 1) tex = if ((connMask & 5) != 0) 0 else 1
    else if (connMask == 5) tex = 0
    else if (connMask == 10) tex = 1
    else tex = 2
    var verts: Array[Vertex5] = Array[Vertex5](new Vertex5(0.5 - w, h, 0.5 + w, 8 - tw, 16 + tw), new Vertex5(0.5 + w, h, 0.5 + w, 8 + tw, 16 + tw), new Vertex5(0.5 + w, h, 0.5 - w, 8 + tw, 16 - tw), new Vertex5(0.5 - w, h, 0.5 - w, 8 - tw, 16 - tw))
    if (tex == 0 || tex == 1) tex = (tex + RenderFlatWire.reorientSide(side)) % 2
    var r: Int = RenderFlatWire.reorientSide(side)
    if (tex == 1) r += 3
    if (r != 0)
    {
      val uvt: UVTransformation = new RenderFlatWire.UVT(Rotation.quarterRotations(r % 4).at(new Vector3(8, 0, 16)))
      for (vert <- verts) vert.apply(uvt)
    }
    if (tex == 2)
    {
      val uvt: UVTranslation = new UVTranslation(16, 0)
      for (vert <- verts) vert.apply(uvt)
    }
    if (inv) verts = withBottom(verts, 0, 4)
    i = RenderFlatWire.addVerts(model, verts, i)
  }

  private def reflectSide(verts: Array[Vertex5], r: Int)
  {
    if ((r + RenderFlatWire.reorientSide(side)) % 4 >= 2) for (vert <- verts) vert.apply(WireModelGenerator.sideReflect)
  }

  /**
   * Returns a copy of vertices with the bottom face added at the start.
   *
   * @param start The index of the first vertex making up the top face
   * @param count The number of vertices making up the top face
   */
  private def withBottom(verts: Array[Vertex5], start: Int, count: Int): Array[Vertex5] =
  {
    val i_verts: Array[Vertex5] = new Array[Vertex5](verts.length + count)
    val r: Transformation = new Rotation(MathHelper.pi, 0, 0, 1).at(new Vector3(0.5, h / 2, 0))

    for (i <- 0 until count)
    {
      i_verts(i) = verts(i + start).copy.apply(r)
    }

    System.arraycopy(verts, 0, i_verts, count, verts.length)
    return i_verts
  }
}
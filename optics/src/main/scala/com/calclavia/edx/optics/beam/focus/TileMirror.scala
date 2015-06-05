package com.calclavia.edx.electrical.circuit.component.laser.focus

import com.calclavia.edx.electric.circuit.component.laser.LaserHandler
import nova.core.util.Direction

import scala.collection.convert.wrapAsJava._

object TileMirror
{
  @SideOnly(Side.CLIENT) val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "mirror.tcn"))
  @SideOnly(Side.CLIENT) val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "mirror.png")
}

/**
 * A mirror reflects lasers.
 *
 * TODO: Make it actually reflect light (render reflection)
 *
 * @author Calclavia
 */
class TileMirror extends TileFocus(Material.glass) with LaserHandler with IFocus
{
	private var normal = new Vector3d(0, 1, 0)
	private var cachedHits = List[Vector3d]()

  domain = ""
  textureName = "stone"
  normalRender = false
  isOpaqueCube = false

  override def update()
  {
    if (isPowered)
    {
      for (a <- 0 to 5)
      {
        val dir = Direction.getOrientation(a)
	      val axis = new Vector3d(dir)
        val rotateAngle = world.getIndirectPowerLevelTo(x + axis.x.toInt, y + axis.y.toInt, z + axis.z.toInt, a) * 15

        if (rotateAngle > 0)
        {
          normal = normal.transform(new Quaternion(Math.toRadians(rotateAngle), axis)).normalize
        }
      }

      world.markBlockForUpdate(x, y, z)
    }

    if (world.getTotalWorldTime % 20 == 0)
      cachedHits = List()
  }

	override def focus(newPosition: Vector3d)
  {
    normal = ((newPosition - position) - 0.5).normalize
    world.markBlockForUpdate(x, y, z)
  }

	override def getFocus: Vector3d = normal

	def setFocus(focus: Vector3d)
  {
    normal = focus
  }

	override def getCacheDirections: java.util.List[Vector3d] = cachedHits.toList

	override def onLaserHit(renderStart: Vector3d, incidentDirection: Vector3d, hit: MovingObjectPosition, color: Vector3d, energy: Double): Boolean =
  {
    /**
     * Cache hits
     */
    cachedHits = incidentDirection :: cachedHits

    /**
     * Render incoming laser
     */
    Electrodynamics.proxy.renderLaser(worldObj, renderStart, position + 0.5, color, energy)

    /**
     * Calculate Reflection
     */
    val angle = Math.acos(incidentDirection $ normal)

    val axisOfReflection = incidentDirection.cross(normal)
    val rotateAngle = 2 * angle - Math.PI

    if (rotateAngle < Math.PI)
    {
      val newDirection = (incidentDirection.clone.transform(new Quaternion(rotateAngle, axisOfReflection))).normalize
      Laser.spawn(worldObj, position + 0.5 + newDirection * 0.9, position + 0.5, newDirection, color, energy / 1.2)
    }

    return true
  }

  override def getDescriptionPacket: Packet =
  {
    val nbt = new NBTTagCompound()
    writeToNBT(nbt)
    return new S35PacketUpdateTileEntity(x, y, z, 0, nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    val normalNBT = new NBTTagCompound()
    normal.writeNBT(normalNBT)
    nbt.setTag("normal", normalNBT)
  }

  override def onDataPacket(net: NetworkManager, pkt: S35PacketUpdateTileEntity)
  {
    val receive = pkt.func_148857_g
    readFromNBT(receive)
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
	  normal = new Vector3d(nbt.getCompoundTag("normal"))
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3d, frame: Float, pass: Int)
  {
    glPushMatrix()
    glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(TileMirror.texture)

    val angle = normal.toEulerAngle
    glRotated(angle.yaw, 0, 1, 0)
    glRotated(angle.pitch, 1, 0, 0)
    glRotated(90, 1, 0, 0)
    TileMirror.model.renderOnly("mirror", "mirrorBacking", "standConnector")

    glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    glPushMatrix()

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(TileMirror.texture)
    TileMirror.model.renderAll()

    glPopMatrix()
  }
}

package resonantinduction.electrical.laser.focus

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.util.{MovingObjectPosition, ResourceLocation}
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11._
import resonantinduction.core.{Reference, ResonantInduction}
import resonantinduction.electrical.laser.{ILaserHandler, Laser}
import resonant.lib.transform.rotation.Quaternion
import resonant.lib.transform.vector.Vector3

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
class TileMirror extends TileFocus(Material.glass) with ILaserHandler with IFocus
{
  private var normal = new Vector3(0, 1, 0)
  private var cachedHits = List[Vector3]()

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
        val dir = ForgeDirection.getOrientation(a)
        val axis = new Vector3(dir)
        val rotateAngle = world.getIndirectPowerLevelTo(xi + axis.x.toInt, yi + axis.y.toInt, zi + axis.z.toInt, a) * 15

        if (rotateAngle > 0)
        {
          normal = normal.transform(new Quaternion(Math.toRadians(rotateAngle), axis)).normalize
        }
      }

      world.markBlockForUpdate(xi, yi, zi)
    }

    if (world.getTotalWorldTime % 20 == 0)
      cachedHits = List()
  }

  override def focus(newPosition: Vector3)
  {
    normal = ((newPosition - toVector3) - 0.5).normalize
    world.markBlockForUpdate(xi, yi, zi)
  }

  override def getFocus: Vector3 = normal

  def setFocus(focus: Vector3)
  {
    normal = focus
  }

  override def getCacheDirections: java.util.List[Vector3] = cachedHits.toList

  override def onLaserHit(renderStart: Vector3, incidentDirection: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double): Boolean =
  {
    /**
     * Cache hits
     */
    cachedHits = incidentDirection :: cachedHits

    /**
     * Render incoming laser
     */
    ResonantInduction.proxy.renderLaser(worldObj, renderStart, toVector3 + 0.5, color, energy)

    /**
     * Calculate Reflection
     */
    val angle = Math.acos(incidentDirection $ normal)

    val axisOfReflection = incidentDirection.cross(normal)
    val rotateAngle = 2 * angle - Math.PI

    if (rotateAngle < Math.PI)
    {
      val newDirection = (incidentDirection.clone.transform(new Quaternion(rotateAngle, axisOfReflection))).normalize
      Laser.spawn(worldObj, toVector3 + 0.5 + newDirection * 0.9, toVector3 + 0.5, newDirection, color, energy / 1.2)
    }

    return true
  }

  override def getDescriptionPacket: Packet =
  {
    val nbt = new NBTTagCompound()
    writeToNBT(nbt)
    return new S35PacketUpdateTileEntity(xi, yi, zi, 0, nbt)
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
    normal = new Vector3(nbt.getCompoundTag("normal"))
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
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

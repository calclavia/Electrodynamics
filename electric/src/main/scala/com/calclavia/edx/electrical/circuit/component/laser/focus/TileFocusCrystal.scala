package com.calclavia.edx.electrical.circuit.component.laser.focus

import com.calclavia.edx.electrical.circuit.component.laser.{Laser, ILaserHandler}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.{Electrodynamics, Reference}
import edx.electrical.circuit.component.laser.Laser
import net.minecraft.block.material.Material
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.util.{MovingObjectPosition, ResourceLocation}
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11._
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.transform.rotation.Quaternion
import resonantengine.lib.transform.vector.Vector3

/**
 * Redirects lasers to one point
 *
 * @author Calclavia
 */
object TileFocusCrystal
{
  @SideOnly(Side.CLIENT) val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "focusCrystal.tcn"))
  @SideOnly(Side.CLIENT) val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "focusCrystal.png")
}

class TileFocusCrystal extends TileFocus(Material.rock) with ILaserHandler with IFocus
{
  //TODO: FIX ITEM RENDERING BY USING ISIMPLEITEMRENDERER
  private var normal = new Vector3(0, 1, 0)
  private var energy = 0D
  private var color = new Vector3(1, 1, 1)

  domain = ""
  textureName = "glass"
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
        val rotateAngle = world.getIndirectPowerLevelTo(x + axis.x.toInt, y + axis.y.toInt, z + axis.z.toInt, a) * 15

        if (rotateAngle > 0)
        {
          normal = normal.transform(new Quaternion(Math.toRadians(rotateAngle), axis)).normalize
        }
      }

      world.markBlockForUpdate(x, y, z)
    }

    if (energy > 0)
    {
      Laser.spawn(worldObj, position + 0.5 + normal * 0.9, position + 0.5, normal, color, energy)
      color = new Vector3(1, 1, 1)
      energy = 0
    }
  }

  override def focus(newPosition: Vector3)
  {
    normal = ((newPosition - position) - 0.5).normalize
    world.markBlockForUpdate(x, y, z)
  }

  override def getFocus: Vector3 = normal

  def setFocus(focus: Vector3)
  {
    normal = focus
  }

  override def getCacheDirections: java.util.List[Vector3] = null

  override def onLaserHit(renderStart: Vector3, incidentDirection: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double): Boolean =
  {
    Electrodynamics.proxy.renderLaser(worldObj, renderStart, position + 0.5, color, energy)
    this.energy += energy
    this.color = (this.color + color) / 2
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
    normal = new Vector3(nbt.getCompoundTag("normal"))
  }

  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    glPushMatrix()
    glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

    RenderUtility.enableBlending()

    val angle = normal.toEulerAngle
    glRotated(angle.yaw, 0, 1, 0)
    glRotated(angle.pitch, 1, 0, 0)

    glRotated(180, 0, 1, 0)

    RenderUtility.bind(TileFocusCrystal.texture)
    glTranslatef(0, 0, 0.08f)
    glScalef(1.3f, 1.3f, 1.3f)
    TileFocusCrystal.model.renderAll()

    RenderUtility.disableBlending()

    glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    glPushMatrix()
    glRotated(180, 0, 1, 0)
    RenderUtility.enableBlending()

    glScaled(2.2, 2.2, 2.2)
    RenderUtility.bind(TileFocusCrystal.texture)
    TileFocusCrystal.model.renderAll()

    RenderUtility.disableBlending()
    glPopMatrix()
  }
}

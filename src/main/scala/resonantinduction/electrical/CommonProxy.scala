package resonantinduction.electrical

import java.awt.Color
import cpw.mods.fml.common.network.IGuiHandler
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.electrical.multimeter.ContainerMultimeter
import resonantinduction.electrical.multimeter.PartMultimeter
import universalelectricity.core.transform.vector.Vector3
import codechicken.multipart.TMultiPart
import codechicken.multipart.TileMultipart

class CommonProxy extends IGuiHandler {
  def preInit {
  }

  def init {
  }

  def postInit {
  }

  def getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    val tileEntity: TileEntity = world.getTileEntity(x, y, z)
    if (tileEntity.isInstanceOf[TileMultipart]) {
      val part: TMultiPart = (tileEntity.asInstanceOf[TileMultipart]).partMap(id)
      if (part.isInstanceOf[PartMultimeter]) {
        return new ContainerMultimeter(player.inventory, (part.asInstanceOf[PartMultimeter]))
      }
    }
    return null
  }

  def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    return null
  }

  def renderElectricShock(world: World, start: Vector3, target: Vector3, r: Float, g: Float, b: Float, split: Boolean) {
  }

  def renderElectricShock(world: World, start: Vector3, target: Vector3, r: Float, g: Float, b: Float) {
    this.renderElectricShock(world, start, target, r, g, b, true)
  }

  def renderElectricShock(world: World, start: Vector3, target: Vector3, color: Color) {
    this.renderElectricShock(world, start, target, color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f)
  }

  def renderElectricShock(world: World, start: Vector3, target: Vector3, color: Color, split: Boolean) {
    this.renderElectricShock(world, start, target, color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f, split)
  }

  def renderElectricShock(world: World, start: Vector3, target: Vector3) {
    this.renderElectricShock(world, start, target, true)
  }

  def renderElectricShock(world: World, start: Vector3, target: Vector3, b: Boolean) {
    this.renderElectricShock(world, start, target, 0.55f, 0.7f, 1f, b)
  }

  def renderBlockParticle(world: World, position: Vector3, block: Block, side: Int)
  {

  }

  def renderLaser(world: World, start: Vector3, end: Vector3, color: Vector3, energy: Double)
  {

  }

  def renderScorch(world: World, position: Vector3, side: Int)
  {

  }
}
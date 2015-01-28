package mffs.base

import java.util.{List => JList, Set => JSet}

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import nova.core.util.transform.{Cuboid, Vector3d}
import resonantengine.api.mffs.machine.{IFieldMatrix, IProjector}
import resonantengine.api.mffs.modules.IModule
import resonantengine.lib.utility.LanguageUtility
import resonantengine.lib.utility.science.UnitDisplay
import resonantengine.lib.wrapper.CollectionWrapper._
import resonantengine.nova.wrapper._

import scala.collection.convert.wrapAll._

class ItemModule extends ItemMFFS with IModule
{
  private var fortronCost = 0.5f

  override def addInformation(itemStack: ItemStack, player: EntityPlayer, info: JList[_], b: Boolean)
  {
    info.add(LanguageUtility.getLocal("info.item.fortron") + " " + new UnitDisplay(UnitDisplay.Unit.LITER, getFortronCost(1) * 20) + "/s")
    super.addInformation(itemStack, player, info, b)
  }

  override def getFortronCost(amplifier: Float): Float =
  {
    return this.fortronCost
  }

  override def onPreCalculate(projector: IFieldMatrix, position: JSet[Vector3d])
  {
  }

  override def onPostCalculate(projector: IFieldMatrix, position: JSet[Vector3d])
  {
  }

  override def onProject(projector: IProjector, fields: JSet[Vector3d]): Boolean =
  {
    return false
  }

  override def onProject(projector: IProjector, position: Vector3d): Int =
  {
    return 0
  }

  override def onCollideWithForceField(world: World, x: Int, y: Int, z: Int, entity: Entity, moduleStack: ItemStack): Boolean =
  {
    return true
  }

  def setCost(cost: Float): ItemModule =
  {
    this.fortronCost = cost
    return this
  }

  override def setMaxStackSize(par1: Int): ItemModule =
  {
    super.setMaxStackSize(par1)
    return this
  }

  override def onDestroy(projector: IProjector, field: JSet[Vector3d]): Boolean =
  {
    return false
  }

  override

  def requireTicks(moduleStack: ItemStack): Boolean =
  {
    return false
  }

  def getEntitiesInField(projector: IProjector): Set[Entity] =
  {
    val tile = projector.asInstanceOf[TileEntity]
    val volume = new Cuboid(-projector.getNegativeScale, projector.getPositiveScale + 1) + (tile.getPosition + projector.getTranslation)
    return (tile.getWorldObj.getEntitiesWithinAABB(classOf[Entity], volume.toAABB) map (_.asInstanceOf[Entity])).toSet
  }
}
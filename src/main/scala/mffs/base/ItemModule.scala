package mffs.base

import java.util.{List, Set}

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.api.mffs.modules.IModule
import resonant.api.mffs.{IFieldInteraction, IProjector}
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.vector.Vector3

class ItemModule extends ItemMFFS with IModule
{
  private var fortronCost = 0.5f

  override def addInformation(itemStack: ItemStack, player: EntityPlayer, info: List[_], b: Boolean)
  {
    info.add(LanguageUtility.getLocal("info.item.fortron") + " " + new UnitDisplay(UnitDisplay.Unit.LITER, getFortronCost(1) * 20) + "/s")
    super.addInformation(itemStack, player, info, b)
  }

  override def onPreCalculate(projector: IFieldInteraction, position: Set[Vector3])
  {
  }

  override def onCalculate(projector: IFieldInteraction, position: Set[Vector3])
  {
  }

  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    return false
  }

  override def onProject(projector: IProjector, position: Vector3): Int =
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

  override def getFortronCost(amplifier: Float): Float =
  {
    return this.fortronCost
  }

  override def onDestroy(projector: IProjector, field: Set[Vector3]): Boolean =
  {
    return false
  }

  override

  def requireTicks(moduleStack: ItemStack): Boolean =
  {
    return false
  }

}
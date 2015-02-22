package mffs.base

import java.util.{List => JList, Set => JSet}

import com.resonant.core.prefab.itemblock.TooltipItem
import mffs.api.modules.Module
import nova.core.item.Item

class ItemModule extends Item with TooltipItem with Module
{
  private var fortronCost = 0.5f

	override def addInformation(Item: Item, player: EntityPlayer, info: JList[_], b: Boolean)
  {
    info.add(LanguageUtility.getLocal("info.item.fortron") + " " + new UnitDisplay(UnitDisplay.Unit.LITER, getFortronCost(1) * 20) + "/s")
	  super.addInformation(Item, player, info, b)
  }

  override def getFortronCost(amplifier: Float): Float =
  {
    return this.fortronCost
  }

  override def onPreCalculate(projector: IFieldMatrix, position: JSet[Vector3])
  {
  }

  override def onPostCalculate(projector: IFieldMatrix, position: JSet[Vector3])
  {
  }

  override def onProject(projector: IProjector, fields: JSet[Vector3]): Boolean =
  {
    return false
  }

  override def onProject(projector: IProjector, position: Vector3): Int =
  {
    return 0
  }

	override def onCollideWithForceField(world: World, x: Int, y: Int, z: Int, entity: Entity, moduleStack: Item): Boolean =
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

  override def onDestroy(projector: IProjector, field: JSet[Vector3]): Boolean =
  {
    return false
  }

  override

  def requireTicks(moduleStack: Item): Boolean =
  {
    return false
  }

  def getEntitiesInField(projector: IProjector): Set[Entity] =
  {
    val tile = projector.asInstanceOf[TileEntity]
    val volume = new Cuboid(-projector.getNegativeScale, projector.getPositiveScale + 1) + (new Vector3(tile) + projector.getTranslation)
    return (tile.getWorldObj.getEntitiesWithinAABB(classOf[Entity], volume.toAABB) map (_.asInstanceOf[Entity])).toSet
  }
}
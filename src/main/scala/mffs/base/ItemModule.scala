package mffs.base

import java.util.{List => JList, Optional, Set => JSet}

import com.resonant.core.prefab.itemblock.TooltipItem
import com.resonant.wrapper.lib.utility.science.UnitDisplay
import mffs.api.machine.Projector
import mffs.api.modules.Module
import mffs.field.BlockProjector
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.item.Item
import nova.core.player.Player
import nova.core.util.transform.{Cuboid, Vector3d}

abstract class ItemModule extends Item with TooltipItem with Module {
	private var fortronCost = 0.5f
	private var maxCount = 64

	override def getTooltips(player: Optional[Player]): JList[String] = {
		val tooltips = super.getTooltips(player)
		tooltips.add(Game.instance.languageManager.getLocal("info.item.fortron") + " " + new UnitDisplay(UnitDisplay.Unit.LITER, getFortronCost(1) * 20) + "/s")
		return tooltips
	}

	override def getFortronCost(amplifier: Float): Float = {
		return fortronCost
	}

	def setCost(cost: Float): this.type = {
		this.fortronCost = cost
		return this
	}

	override def getMaxCount: Int = maxCount

	def setMaxCount(maxCount: Int): ItemModule = {
		this.maxCount = maxCount
		return this
	}

	def getEntitiesInField(projector: Projector): JSet[Entity] = {
		val blockProjector = projector.asInstanceOf[BlockProjector]
		val volume = new Cuboid(-projector.getNegativeScale, projector.getPositiveScale + Vector3d.one) + (new Vector3d(blockProjector) + projector.getTranslation)
		return (blockProjector.world.getEntitiesWithinAABB(classOf[Entity], volume.toAABB) map (_.asInstanceOf[Entity])).toSet
	}
}
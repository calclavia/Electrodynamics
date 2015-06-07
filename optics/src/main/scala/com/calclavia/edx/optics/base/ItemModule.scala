package com.calclavia.edx.optics.base

import java.util.{List => JList, Set => JSet}

import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.api.modules.Module
import com.calclavia.edx.optics.field.BlockProjector
import com.resonant.core.prefab.itemblock.TooltipItem
import com.resonant.core.prefab.modcontent.AutoItemTexture
import com.resonant.lib.WrapFunctions._
import com.resonant.wrapper.lib.utility.science.UnitDisplay
import nova.core.entity.Entity
import com.calclavia.edx.core.EDX
import nova.core.item.Item
import nova.core.item.Item.TooltipEvent
import nova.core.util.transform.shape.Cuboid
import nova.core.util.transform.vector.Vector3i

import scala.collection.convert.wrapAll._

abstract class ItemModule extends Item with TooltipItem with Module with AutoItemTexture {
	private var fortronCost = 0.5f
	private var maxCount = 64

	add(new CategoryMFFS)

	tooltipEvent.add(eventListener((evt: TooltipEvent) => evt.tooltips.add(EDX.language.translate("info.item.fortron") + " " + new
			UnitDisplay(UnitDisplay.Unit.LITER, getFortronCost(1) * 20) + "/s")))

	override def getFortronCost(amplifier: Float) = fortronCost

	def setCost(cost: Float): this.type = {
		this.fortronCost = cost
		return this
	}

	override def getMaxCount: Int = maxCount

	def setMaxCount(maxCount: Int): ItemModule = {
		this.maxCount = maxCount
		return this
	}

	def getEntitiesInField(projector: Projector): Set[Entity] = {
		val blockProjector = projector.asInstanceOf[BlockProjector]
		val bound = new Cuboid(-projector.getNegativeScale, projector.getPositiveScale + Vector3i.one) + blockProjector.transform.position + projector.getTranslation

		return blockProjector.world.getEntities(bound)
			.filter(entity => projector.isInField(entity.transform.position))
			.toSet
	}
}
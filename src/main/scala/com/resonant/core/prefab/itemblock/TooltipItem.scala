package com.resonant.core.prefab.itemblock

import nova.core.gui.InputManager.Key
import nova.core.item.Item
import nova.core.item.Item.TooltipEvent
import nova.core.render.Color
import nova.internal.core.Game
import nova.scala.wrapper.FunctionalWrapper._

/**
 * @author Calclavia
 */
trait TooltipItem extends Item {

	events.add(eventListener((evt: TooltipEvent) => {
		val tooltipID = getID + ".tooltip"
		val tooltip = Game.language().translate(tooltipID)

		if (tooltip != null && !tooltip.isEmpty && !tooltip.equals(tooltipID)) {
			//TODO: Bad reference to game?
			if (!Game.input.isKeyDown(Key.KEY_LSHIFT)) {
				evt.tooltips.add(Game.language().translate("tooltip.noShift").replace("#0", Color.blue.toString).replace("#1", Color.gray.toString))
			}
			else {
				//evt.tooltips.addAll(tooltip.listWrap(20))
			}
		}
	}), classOf[TooltipEvent])
}

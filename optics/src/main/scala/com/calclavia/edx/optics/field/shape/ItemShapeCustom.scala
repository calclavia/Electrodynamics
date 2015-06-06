package com.calclavia.edx.optics.field.shape

import java.io.File
import java.util.{Set => JSet}

import com.calclavia.edx.optics.Settings
import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.content.OpticsContent
import com.calclavia.edx.optics.util.CacheHandler
import com.resonant.core.structure.{Structure, StructureCustom}
import com.resonant.lib.WrapFunctions._
import nova.core.game.Game
import nova.core.gui.InputManager.Key
import nova.core.item.Item.{RightClickEvent, TooltipEvent, UseEvent}
import nova.core.render.model.Model
import nova.core.retention.Store
import nova.core.util.transform.vector.Vector3i

import scala.util.Random

class ItemShapeCustom extends ItemShape with CacheHandler {

	private final val saveFilePrefix: String = "custom_mode_"
	val modes = Array(OpticsContent.modeCube, OpticsContent.modeSphere, OpticsContent.modeTube, OpticsContent.modePyramid)
	@Store
	var saveID = -1
	@Store
	var pointA: Vector3i = null
	@Store
	var pointB: Vector3i = null
	@Store
	var isAdditive = true
	@Store
	var fieldSize = 0

	tooltipEvent.add(eventListener((evt: TooltipEvent) => {
		evt.tooltips.add(Game.language.translate("info.modeCustom.mode") + " " + (if (isAdditive) Game.language.translate("info.modeCustom.additive") else Game.language.translate("info.modeCustom.substraction")))
		evt.tooltips.add(Game.language.translate("info.modeCustom.point1") + " " + pointA.xi + ", " + pointA.yi + ", " + pointA.zi)
		evt.tooltips.add(Game.language.translate("info.modeCustom.point2") + " " + pointB.xi + ", " + pointB.yi + ", " + pointB.zi)

		if (saveID > 0) {
			evt.tooltips.add(Game.language.translate("info.modeCustom.modeID") + " " + saveID)
			if (fieldSize > 0) {
				evt.tooltips.add(Game.language.translate("info.modeCustom.fieldSize") + " " + fieldSize)
			}
			else {
				evt.tooltips.add(Game.language.translate("info.modeCustom.notSaved"))
			}
		}

		if (!Game.input.isKeyDown(Key.KEY_LSHIFT)) {
			evt.tooltips.add(Game.language.translate("info.modeCustom.shift"))
		}
	}))

	useEvent.add((evt: UseEvent) => {
		if (Game.network.isServer) {

			if (pointA == null) {
				pointA = evt.position
				//player.addChatMessage(new ChatComponentText("Set point 1: " + x + ", " + y + ", " + z + "."))
			}
			else {
				pointB = evt.position
				//player.addChatMessage(new ChatComponentText("Set point 2: " + x + ", " + y + ", " + z + "."))
			}
		}
		evt.action = true
	})

	rightClickEvent.add((evt: RightClickEvent) => {
		if (Game.network.isServer) {
			if (Game.input.isKeyDown(Key.KEY_LSHIFT)) {
				//Holding shift saves the item

				if (pointA != null && pointB != null && !pointA.equals(pointB)) {
					if (pointA.distance(pointB) < Settings.maxForceFieldScale) {
						val midPoint = pointA.midpoint(pointB)
						val translatedA = pointA - midPoint
						val translatedB = pointB - midPoint
						pointA = null
						pointB = null

						val minPoint = translatedA.min(translatedB)
						val maxPoint = translatedA.max(translatedB)

						val customStructure = new StructureCustom("shapeCustom")

						if (saveID == -1) {
							saveID = getNextAvaliableID
						}

						Game.retention.load(saveFilePrefix + saveID, customStructure)

						for (x <- minPoint.x to maxPoint.x; y <- minPoint.y to maxPoint.y; z <- minPoint.z to maxPoint.z) {
							val position = new Vector3i(x, y, z)
							val targetCheck = midPoint + position
							val block = evt.entity.world().getBlock(targetCheck)

							if (block.isPresent) {
								/**
								 * Additive and Subtractive modes
								 */
								if (isAdditive) {
									customStructure.structure += (position -> block.get().getID)
								}
								else {
									customStructure.structure -= position
								}
							}
						}

						fieldSize = customStructure.structure.size
						//entityPlayer.addChatMessage(new ChatComponentText(Game.get.language.translate("message.modeCustom.saved")))
						Game.retention.queueSave(saveFilePrefix + saveID, customStructure)
						clearCache()
					}
				}

			}
			else {
				isAdditive = !isAdditive
				//entityPlayer.addChatMessage(new ChatComponentText(Game.get.language.translate("message.modeCustom.modeChange").replaceAll("#p", (if (nbt.getBoolean(NBT_MODE)) Game.get.language.translate("info.modeCustom.substraction") else Game.get.language.translate("info.modeCustom.additive")))))
			}
		}
	})

	override def getID: String = "shapeCustom"

	override def getStructure: Structure =
		getOrSetCache("shapeCustom", () => {
			val custom = new StructureCustom("shapeCustom")
			Game.retention.load(saveFilePrefix + saveID, custom)
			return custom
		})

	def getNextAvaliableID: Int = getSaveDirectory.listFiles.length

	def getSaveDirectory: File = {
		val saveDirectory: File = Game.retention.getSaveDirectory
		if (!saveDirectory.exists) {
			saveDirectory.mkdir
		}
		/*
		val file: File = new File(saveDirectory, "mffs")
		if (!file.exists) {
			file.mkdir
		}*/
		return saveDirectory
	}

	override def render(projector: Projector, model: Model) {
		modes(new Random().nextInt(modes.length - 1)).getDummy.asInstanceOf[ItemShape].render(projector, model)
	}

	override def getFortronCost(amplifier: Float): Float = super.getFortronCost(amplifier) * amplifier
}
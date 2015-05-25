package com.calclavia.edx.mffs.field.shape

import java.io.File
import java.util
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.mffs.Settings
import com.calclavia.edx.mffs.api.machine.Projector
import com.calclavia.edx.mffs.content.Content
import com.calclavia.edx.mffs.util.CacheHandler
import com.resonant.core.structure.{Structure, StructureCustom}
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.render.model.Model
import nova.core.retention.Stored
import nova.core.util.Direction
import nova.core.util.transform.vector.{Vector3d, Vector3i}
import nova.core.world.World

import scala.util.Random

class ItemShapeCustom extends ItemShape with CacheHandler {

	private final val saveFilePrefix: String = "custom_mode_"
	val modes = Array(Content.modeCube, Content.modeSphere, Content.modeTube, Content.modePyramid)
	@Stored
	var saveID = -1
	@Stored
	var pointA: Vector3i = null
	@Stored
	var pointB: Vector3i = null
	@Stored
	var isAdditive = true
	@Stored
	var fieldSize = 0

	override def getID: String = "shapeCustom"

	override def getStructure: Structure =
		getOrSetCache("shapeCustom", () => {
			val custom = new StructureCustom("shapeCustom")
			Game.instance.saveManager.load(saveFilePrefix + saveID, custom)
			return custom
		})

	override def getTooltips(player: Optional[Entity], tooltips: util.List[String]) {
		super.getTooltips(player, tooltips)

		tooltips.add(Game.instance.languageManager.translate("info.modeCustom.mode") + " " + (if (isAdditive) Game.instance.languageManager.translate("info.modeCustom.additive") else Game.instance.languageManager.translate("info.modeCustom.substraction")))
		tooltips.add(Game.instance.languageManager.translate("info.modeCustom.point1") + " " + pointA.xi + ", " + pointA.yi + ", " + pointA.zi)
		tooltips.add(Game.instance.languageManager.translate("info.modeCustom.point2") + " " + pointB.xi + ", " + pointB.yi + ", " + pointB.zi)

		if (saveID > 0) {
			tooltips.add(Game.instance.languageManager.translate("info.modeCustom.modeID") + " " + saveID)
			if (fieldSize > 0) {
				tooltips.add(Game.instance.languageManager.translate("info.modeCustom.fieldSize") + " " + fieldSize)
			}
			else {
				tooltips.add(Game.instance.languageManager.translate("info.modeCustom.notSaved"))
			}
		}

		if (Game.instance.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
			super.getTooltips(player, tooltips)
		}
		else {
			tooltips.add(Game.instance.languageManager.translate("info.modeCustom.shift"))
		}
	}

	override def onRightClick(entity: Entity) {
		super.onRightClick(entity)

		if (Game.instance.networkManager.isServer) {
			if (Game.instance.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
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

						Game.instance.saveManager.load(saveFilePrefix + saveID, customStructure)

						for (x <- minPoint.x to maxPoint.x; y <- minPoint.y to maxPoint.y; z <- minPoint.z to maxPoint.z) {
							val position = new Vector3i(x, y, z)
							val targetCheck = midPoint + position
							val block = entity.world().getBlock(targetCheck)

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
						//entityPlayer.addChatMessage(new ChatComponentText(Game.instance.get.languageManager.translate("message.modeCustom.saved")))
						Game.instance.saveManager.queueSave(saveFilePrefix + saveID, customStructure)
						clearCache()
					}
				}

			}
			else {
				isAdditive = !isAdditive
				//entityPlayer.addChatMessage(new ChatComponentText(Game.instance.get.languageManager.translate("message.modeCustom.modeChange").replaceAll("#p", (if (nbt.getBoolean(NBT_MODE)) Game.instance.get.languageManager.translate("info.modeCustom.substraction") else Game.instance.get.languageManager.translate("info.modeCustom.additive")))))
			}
		}
	}

	def getNextAvaliableID: Int = getSaveDirectory.listFiles.length

	def getSaveDirectory: File = {
		val saveDirectory: File = Game.instance.saveManager.getSaveDirectory
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

	override def onUse(entity: Entity, world: World, position: Vector3i, side: Direction, hit: Vector3d): Boolean = {
		if (Game.instance.networkManager.isServer) {

			if (pointA == null) {
				pointA = position
				//player.addChatMessage(new ChatComponentText("Set point 1: " + x + ", " + y + ", " + z + "."))
			}
			else {
				pointB = position
				//player.addChatMessage(new ChatComponentText("Set point 2: " + x + ", " + y + ", " + z + "."))
			}
		}

		return true
	}

	override def render(projector: Projector, model: Model) {
		modes(new Random().nextInt(modes.length - 1)).getDummy.asInstanceOf[ItemShape].render(projector, model)
	}

	override def getFortronCost(amplifier: Float): Float = super.getFortronCost(amplifier) * amplifier
}
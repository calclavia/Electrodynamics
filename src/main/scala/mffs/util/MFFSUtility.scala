package mffs.util

import com.resonant.core.access.Permission
import mffs.GraphFrequency
import mffs.api.machine.Projector
import mffs.content.Content
import mffs.field.BlockProjector
import mffs.field.shape.ItemShapeCustom
import nova.core.block.Block
import nova.core.entity.Entity
import nova.core.inventory.Inventory
import nova.core.inventory.components.InventoryProvider
import nova.core.item.{Item, ItemBlock}
import nova.core.network.NetworkTarget.Side
import nova.core.player.Player
import nova.core.util.Direction
import nova.core.util.transform.{Quaternion, Vector3d, Vector3i}
import nova.core.world.World

import scala.collection.convert.wrapAll._

/**
 * A class containing some general helpful functions.
 *
 * @author Calclavia
 */
object MFFSUtility {

	/**
	 * Gets the first Item that is an ItemBlock in this TileEntity or in nearby chests.
	 */
	def getFirstItemBlock(block: Block, Item: Item, recur: Boolean = true): Item = {
		block match {
			case projector: BlockProjector =>
				val firstItemBlock = projector
					.getModuleSlots()
					.view
					.map(projector.getInventory(Direction.UNKNOWN).get().get)
					.collect { case op if op.isPresent => op.get()}
					.collect { case item: ItemBlock => item}
					.headOption
					.orNull

				if (firstItemBlock != null) {
					return firstItemBlock
				}
			case invProvider: InventoryProvider =>
				Direction.DIRECTIONS
					.view
					.collect { case dir if invProvider.getInventory(dir).isPresent => invProvider.getInventory(dir).get()}
					.flatten
					.headOption match {
					case Some(entry) => return entry
					case _ =>
				}
		}

		if (recur) {
			Direction.DIRECTIONS.foreach(
				direction => {
					val checkPos = block.position() + direction.toVector
					val checkBlock = block.blockAccess().getBlock(checkPos)

					if (checkBlock.isPresent) {
						val checkStack = getFirstItemBlock(checkBlock.get(), Item, false)

						if (checkStack != null) {
							return checkStack
						}
					}
				})
		}
		return null
	}

	def getFirstItemBlock(i: Int, inventory: Inventory, Item: Item): Item = {
		return inventory.toSet.headOption.orNull
	}

	def getCamoBlock(proj: Projector, position: Vector3i): Block = {
		val projector = proj.asInstanceOf[BlockProjector]

		if (projector != null) {
			if (Side.get().isServer) {
				if (projector.getModuleCount(Content.moduleCamouflage) > 0) {
					if (projector.getShapeItem.isInstanceOf[ItemShapeCustom]) {
						val fieldMap = projector.getShapeItem.asInstanceOf[ItemShapeCustom].getStructure

						if (fieldMap != null) {
							val fieldCenter = projector.position() + projector.getTranslation()
							var relativePosition = position - fieldCenter
							relativePosition = relativePosition.transform(Quaternion.fromEuler(-projector.getRotationYaw, -projector.getRotationPitch, 0))

							val theBlock = fieldMap.getBlock(relativePosition)

							if (theBlock.isPresent) {
								return theBlock.get()
							}
						}
					}

					projector.getFilterItems
						.filter(getFilterBlock(_) != null)
						.collect { case item: ItemBlock => item.block}
						.headOption match {
						case Some(block) => return block
						case _ => return null
					}
				}
			}
		}

		return null
	}

	def getFilterBlock(item: Item): Block = {
		if (item.isInstanceOf[ItemBlock]) {
			return item.asInstanceOf[ItemBlock].block

		}
		return null
	}

	def hasPermission(world: World, position: Vector3d, permission: Permission, player: Entity with Player): Boolean =
		hasPermission(world, position, permission, player.getID())

	/*
		def hasPermission(world: World, position: Vector3d, action: PlayerInteractEvent.Action, player: EntityPlayer): Boolean =
			getRelevantProjectors(world, position) forall (_.isAccessGranted(world, position, player, action))
	*/

	/**
	 * Gets the set of projectors that have an intersect in this position.
	 */
	def getRelevantProjectors(world: World, position: Vector3d): Set[BlockProjector] =
		GraphFrequency
			.instance
			.nodes
			.collect { case proj: BlockProjector if proj.world.equals(world) => proj}
			.filter(_.isInField(position))
			.toSet

	def hasPermission(world: World, position: Vector3d, permission: Permission, id: String): Boolean =
		getRelevantProjectors(world, position)
			.forall(_.hasPermission(id, permission))

}
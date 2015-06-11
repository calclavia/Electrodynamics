package com.calclavia.edx.core.content

import java.util.Optional

import com.calclavia.edx.core.CoreContent
import com.resonant.core.structure.Structure
import nova.core.block.Block
import nova.core.block.Block.RightClickEvent
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.Category
import nova.core.component.transform.Orientation
import nova.core.network.{Packet, Syncable}
import nova.core.util.Direction
import nova.internal.core.Game
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._

object BlockCreativeBuilder {
	var schematics: Seq[Structure] = Seq.empty
}

class BlockCreativeBuilder extends Block with Syncable {

	add(new Orientation(this).setMask(0x3F))

	add(new StaticBlockRenderer(this).setTexture(func((dir: Direction) => Optional.of(CoreContent.textureCreativeBuilder))))

	add(new Category("tools"))

	events.add((evt: RightClickEvent) => onRightClick(evt), classOf[RightClickEvent])

	/**
	 * Called when the block is right clicked by the player
	 */
	def onRightClick(evt: RightClickEvent) {
		Game.gui.showGui("creativeBuilder", evt.entity, transform.position)
		evt.result = true
	}

	override def read(packet: Packet) {
		super.read(packet)
		if (Game.network.isServer && packet.getID == 1) {
			val schematicID = packet.readInt
			val size = packet.readInt
			val buildMap = BlockCreativeBuilder.schematics(schematicID).getBlockStructure
			buildMap.foreach(kv => {
				val placement = transform.position + kv._1
				world.setBlock(placement, kv._2)
			})
		}
	}

	override def getID: String = "creativeBuilder"
}
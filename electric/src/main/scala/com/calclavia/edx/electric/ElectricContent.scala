package com.calclavia.edx.electric

import com.calclavia.edx.core.{EDX, Reference}
import com.calclavia.edx.electric.circuit.component.BlockSiren
import com.calclavia.edx.electric.circuit.source.{BlockBattery, BlockSolarPanel, BlockThermopile, ItemBlockBattery}
import com.calclavia.edx.electric.circuit.wire.BlockWire
import nova.core.block.{Block, BlockFactory}
import nova.core.item.Item
import nova.core.render.model.TechneModelProvider
import nova.core.render.texture.BlockTexture
import nova.scala.modcontent.ContentLoader
import nova.scala.wrapper.FunctionalWrapper._

/**
 * @author Calclavia
 */
object ElectricContent extends ContentLoader {

	/**
	 * Textures
	 */
	val wireTexture = new BlockTexture(Reference.domain, "wire")
	val batteryTexture = new BlockTexture(Reference.domain, "battery")
	val solarPanelTextureTop = new BlockTexture(Reference.domain, "solarPanel_top")
	val solarPanelTextureSide = new BlockTexture(Reference.domain, "solarPanel_side")
	val solarPanelTextureBottom = new BlockTexture(Reference.domain, "solarPanel_bottom")
	val solarPanelTextureEdge = new BlockTexture(Reference.domain, "connectEdge")
	val thermopileTextureTop = new BlockTexture(Reference.domain, "material_metal_top")
	val thermopileTextureSide = new BlockTexture(Reference.domain, "thermopile")
	val sirenTexture = new BlockTexture(Reference.domain, "siren")

	/**
	 * Blocks
	 */
	val battery: BlockFactory = new BlockFactory(
		() => new BlockBattery,
		func((b: Block) => {
			EDX.items.register(supplier[Item](() => new ItemBlockBattery(b.factory())))
			b
		})
	)

	val wire: BlockFactory = classOf[BlockWire]
	val thermopile: BlockFactory = classOf[BlockThermopile]
	val solarPanel: BlockFactory = classOf[BlockSolarPanel]
	val siren: BlockFactory = classOf[BlockSiren]

	/**
	 * Items
	 */

	/**
	 * Models
	 */
	val batteryModel = new TechneModelProvider(Reference.domain, "battery")

	override def id: String = Reference.electricID
}

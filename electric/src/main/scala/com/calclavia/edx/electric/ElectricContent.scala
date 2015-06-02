package com.calclavia.edx.electric

import com.calclavia.edx.core.Reference
import com.calclavia.edx.electric.circuit.source.BlockBattery
import com.calclavia.edx.electric.circuit.wire.BlockWire
import com.resonant.core.prefab.modcontent.ContentLoader
import nova.core.block.BlockFactory
import nova.core.render.model.TechneModel
import nova.core.render.texture.BlockTexture

/**
 * @author Calclavia
 */
object ElectricContent extends ContentLoader {
	/**
	 * Blocks
	 */
	val wire: BlockFactory = classOf[BlockWire]
	val battery: BlockFactory = classOf[BlockBattery]

	/**
	 * Items
	 */

	/**
	 * Textures
	 */
	val wireTexture = new BlockTexture(Reference.domain, "wire")
	val batteryTexture = new BlockTexture(Reference.domain, "battery")
	val solarPanelTextureSide = new BlockTexture(Reference.domain, "solarPanel_side")
	val solarPanelTextureBottom = new BlockTexture(Reference.domain, "solarPanel_bottom")
	val solarPanelTextureEdge = new BlockTexture(Reference.domain, "solarPanelEdge")
	val thermopileTexture = new BlockTexture(Reference.domain, "thermopile_top")

	/**
	 * Models
	 */
	val batteryModel = new TechneModel(Reference.domain, "battery")

	override def id: String = Reference.electricID
}

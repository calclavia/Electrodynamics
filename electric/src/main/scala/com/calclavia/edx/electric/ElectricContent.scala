package com.calclavia.edx.electric

import com.calclavia.edx.core.Reference
import com.calclavia.edx.electric.circuit.component.BlockSiren
import com.calclavia.edx.optics.beam.{BlockLaserEmitter, BlockLaserReceiver}
import com.calclavia.edx.electric.circuit.source.{BlockBattery, BlockSolarPanel, BlockThermopile}
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
	val battery: BlockFactory = classOf[BlockBattery]
	val wire: BlockFactory = classOf[BlockWire]
	val thermopile: BlockFactory = classOf[BlockThermopile]
	val solarPanel: BlockFactory = classOf[BlockSolarPanel]
	val siren: BlockFactory = classOf[BlockSiren]

	/**
	 * Items
	 */

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
	 * Models
	 */
	val batteryModel = new TechneModel(Reference.domain, "battery")

	override def id: String = Reference.electricID
}

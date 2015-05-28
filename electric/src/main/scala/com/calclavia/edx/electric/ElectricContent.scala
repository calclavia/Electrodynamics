package com.calclavia.edx.electric

import com.calclavia.edx.core.Reference
import com.calclavia.edx.electric.circuit.wire.BlockWire
import com.resonant.core.prefab.modcontent.ContentLoader
import nova.core.block.BlockFactory
import nova.core.render.texture.BlockTexture

/**
 * @author Calclavia
 */
object ElectricContent extends ContentLoader {
	/**
	 * Blocks
	 */
	val wire: BlockFactory = classOf[BlockWire]
	/**
	 * Textures
	 */
	val wireTexture = new BlockTexture(Reference.domain, "wire")

	/**
	 * Items
	 */

	override def id: String = Reference.electricID
}

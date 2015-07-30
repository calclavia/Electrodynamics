package com.calclavia.edx.mechanical

import com.calclavia.edx.core.Reference
import com.calclavia.edx.mechanical.content.gear.BlockGear
import com.calclavia.edx.mechanical.content.axle.BlockAxle
import nova.core.block.BlockFactory
import nova.core.render.model.WavefrontObjectModelProvider
import nova.core.render.texture.BlockTexture
import nova.scala.modcontent.ContentLoader

object MechanicContent extends ContentLoader {
	override def id: String = Reference.mechanicID

	//Textures
	val gearTexture = new BlockTexture(Reference.domain, "wire")
	lazy val gearshaftTexture = gearTexture

	//Models
	//val modelGear = new WavefrontObjectModelProvider(Reference.domain, "gears")

	//Blocks
	var blockGear: BlockFactory = classOf[BlockGear]
	var blockGearshaft: BlockFactory = classOf[BlockAxle]
}

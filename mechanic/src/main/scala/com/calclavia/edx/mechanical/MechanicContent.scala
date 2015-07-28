package com.calclavia.edx.mechanical

import com.calclavia.edx.core.Reference
import com.calclavia.edx.mechanical.mech.gear.BlockGear
import com.calclavia.edx.mechanical.mech.gearshaft.BlockGearshaft
import nova.core.block.BlockFactory
import nova.core.render.texture.BlockTexture
import nova.scala.modcontent.ContentLoader


object MechanicContent extends ContentLoader{
	override def id: String = Reference.mechanicID
	val gearTexture = new BlockTexture(Reference.domain, "wire")
	lazy val gearshaftTexture = gearTexture
	//Blocks
	var blockGear: BlockFactory = classOf[BlockGear]
	var blockGearshaft: BlockFactory = classOf[BlockGearshaft]
}

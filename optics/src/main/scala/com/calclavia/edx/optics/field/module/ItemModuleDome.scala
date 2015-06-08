package com.calclavia.edx.optics.field.module

import com.calclavia.edx.optics.api.machine.FieldMatrix
import com.calclavia.edx.optics.base.ItemModule
import com.resonant.core.structure.Structure

class ItemModuleDome extends ItemModule {
	override def getID: String = "moduleDome"

	override def getMaxCount: Int = 1

	override def onCalculateInterior(projector: FieldMatrix, structure: Structure) {
		//Cuts the field in half.
		structure.postMapper = structure.postMapper.andThen({
			case vec if vec.getY() > 0 => vec
		})
	}
}
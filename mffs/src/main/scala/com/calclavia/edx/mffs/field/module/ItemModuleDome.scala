package com.calclavia.edx.mffs.field.module

import com.calclavia.edx.mffs.api.machine.FieldMatrix
import com.calclavia.edx.mffs.base.ItemModule
import com.resonant.core.structure.Structure

class ItemModuleDome extends ItemModule {
	override def getID: String = "moduleDome"

	override def getMaxCount: Int = 1

	override def onCalculateInterior(projector: FieldMatrix, structure: Structure) {
		//Cuts the field in half.
		structure.postMapper = structure.postMapper.andThen({
			case vec if vec.y > 0 => vec
		})
	}
}
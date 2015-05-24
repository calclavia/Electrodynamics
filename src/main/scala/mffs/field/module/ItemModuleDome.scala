package mffs.field.module

import com.resonant.core.structure.Structure
import mffs.api.machine.FieldMatrix
import mffs.base.ItemModule

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
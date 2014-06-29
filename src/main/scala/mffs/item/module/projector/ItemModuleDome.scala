package mffs.item.module.projector

import java.util.Set
import mffs.item.module.ItemModule
import net.minecraft.tileentity.TileEntity
import universalelectricity.core.transform.vector.Vector3
import resonant.api.mffs.IFieldInteraction
import scala.collection.convert.wrapAll._

class ItemModuleDome(id: Int) extends ItemModule(id, "moduleDome")
{
	setMaxStackSize(1)

	override def onCalculate(projector: IFieldInteraction, fieldBlocks: Set[Vector3])
	{
		val absoluteTranslation = new Vector3(projector.asInstanceOf[TileEntity]).translate(projector.getTranslation)
		val newField = fieldBlocks.par.filter(_.y > absoluteTranslation.y).seq
		fieldBlocks.clear()
		fieldBlocks.addAll(newField)
	}
}
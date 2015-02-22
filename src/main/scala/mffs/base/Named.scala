package mffs.base

import nova.core.util.Identifiable

/**
 * @author Calclavia
 */
trait Named extends Identifiable {
	var name = ""

	def setName(name: String): this.type = {
		this.name = name
		return this
	}

	override def getID: String = name
}

package nova.resources

import nova.core.util.Identifiable

/**
 * A class used by rubble, dusts and refined dusts
 * @author Calclavia
 */
trait Resource extends Identifiable {
	var id: String = ""
	var material: String = ""

	override def getID: String = id
}

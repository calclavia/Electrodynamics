package mffs.field.mode

import com.resonant.core.structure.Structure
import mffs.api.machine.Projector
import mffs.field.structure.StructureCylinder
import nova.core.render.model.{BlockModelUtil, Model}
import nova.core.util.transform.Vector3d

/**
 * A cylinder mode.
 *
 * @author Calclavia, Thutmose
 */
class ItemShapeCylinder extends ItemShape {
	private val step = 1
	private val radiusExpansion: Int = 0

	override def getID: String = "modeCylinder"

	override def getStructure: Structure = new StructureCylinder

	override def render(projector: Projector, model: Model) {
		val scale = 0.15f
		val detail = 0.5f
		val radius = (1.5f * detail).toInt

		model.scale(scale, scale, scale)

		var i = 0

		for (renderX <- -radius to radius; renderY <- -radius to radius; renderZ <- -radius to radius) {
			if (((renderX * renderX + renderZ * renderZ + radiusExpansion) <= (radius * radius) && (renderX * renderX + renderZ * renderZ + radiusExpansion) >= ((radius - 1) * (radius - 1))) || ((renderY == 0 || renderY == radius - 1) && (renderX * renderX + renderZ * renderZ + radiusExpansion) <= (radius * radius))) {
				if (i % 2 == 0) {
					val vector = new Vector3d(renderX / detail, renderY / detail, renderZ / detail)
					val cube = BlockModelUtil.drawCube(new Model())
					cube.translate(vector.x, vector.y, vector.z)
					model.children.add(cube)
				}
				i += 1
			}
		}
	}
}
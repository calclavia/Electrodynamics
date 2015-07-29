package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.content.OpticsTextures
import com.calclavia.edx.optics.field.structure.StructureCylinder
import com.resonant.core.structure.Structure
import nova.core.render.model.{Model, VertexModel}
import nova.core.render.pipeline.BlockRenderer
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

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

	renderer.setOnRender(
		(model: Model) => {
			val scale = 0.15f
			val detail = 0.5f
			val radius = (1.5f * detail).toInt

			model.matrix.scale(scale, scale, scale)

			var i = 0

			for (renderX <- -radius to radius; renderY <- -radius to radius; renderZ <- -radius to radius) {
				if (((renderX * renderX + renderZ * renderZ + radiusExpansion) <= (radius * radius) && (renderX * renderX + renderZ * renderZ + radiusExpansion) >= ((radius - 1) * (radius - 1))) || ((renderY == 0 || renderY == radius - 1) && (renderX * renderX + renderZ * renderZ + radiusExpansion) <= (radius * radius))) {
					if (i % 2 == 0) {
						val vector = new Vector3D(renderX / detail, renderY / detail, renderZ / detail)
						val cube = BlockRenderer.drawCube(new VertexModel())
						cube.matrix.translate(vector.getX(), vector.getY(), vector.getZ())
						model.children.add(cube)
						cube.bindAll(OpticsTextures.hologram)
					}
					i += 1
				}
			}
		})
}
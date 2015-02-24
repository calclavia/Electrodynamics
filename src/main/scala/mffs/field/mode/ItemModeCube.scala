package mffs.field.mode

import java.util.{HashSet, Set}
import javax.vecmath.Vector3d

import com.resonant.wrapper.lib.schematic.{Structure, StructureCube}
import mffs.api.machine.Projector
import nova.core.render.model.{BlockModelUtil, Model}
import nova.core.util.transform.{Cuboid, MatrixStack}

class ItemModeCube extends ItemMode {
	private val step = 1

	def getExteriorPoints(projector: IFieldMatrix): Set[Vector3d] = {
		val fieldBlocks = new HashSet[Vector3d]
		val posScale: Vector3d = projector.getPositiveScale
		val negScale: Vector3d = projector.getNegativeScale

		for (x <- -negScale.xi to posScale.xi by step; y <- -negScale.yi to posScale.yi by step; z <- -negScale.zi to posScale.zi by step)
			if (y == -negScale.yi || y == posScale.yi || x == -negScale.xi || x == posScale.xi || z == -negScale.zi || z == posScale.zi) {
				fieldBlocks.add(new Vector3d(x, y, z))
			}


		return fieldBlocks
	}

	def getInteriorPoints(projector: IFieldMatrix): Set[Vector3d] = {
		val fieldBlocks = new HashSet[Vector3d]
		val posScale = projector.getPositiveScale
		val negScale = projector.getNegativeScale

		//TODO: Check parallel possibility
		for (x <- -negScale.xi to posScale.xi by step; y <- -negScale.yi to posScale.yi by step; z <- -negScale.zi to posScale.zi by step)
			fieldBlocks.add(new Vector3d(x, y, z))

		return fieldBlocks
	}

	override def isInField(projector: IFieldMatrix, position: Vector3d): Boolean = {
		val projectorPos: Vector3d = new Vector3d(projector.asInstanceOf[TileEntity])
		projectorPos.add(projector.getTranslation)
		val relativePosition = position.clone.subtract(projectorPos)
		relativePosition.transform(new EulerAngle(-projector.getRotationYaw, -projector.getRotationPitch, 0))
		val region = new Cuboid(-projector.getNegativeScale, projector.getPositiveScale)
		return region.intersects(relativePosition)
	}

	override def getID: String = "modeCube"

	override def getStructure: Structure = new StructureCube

	override def render(projector: Projector, model: Model) {
		model.matrix = new MatrixStack().loadMatrix(model.matrix).scale(0.5, 0.5, 0.5).getMatrix
		BlockModelUtil.drawCube(model)
	}
}
package com.calclavia.edx.optics.field.structure

import java.lang.Math.abs

import com.resonant.core.structure.StructureCube
import nova.core.util.Profiler
import nova.core.util.math.MathUtil._
import nova.core.util.math.Vector3DUtil
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * @author Calclavia
 */
class ShapeTest {
	@Test
	def testCube() {
		val struct = new StructureCube

		for (scale <- 1 to 500 by 25) {
			val extStructProf = new Profiler("Cube " + scale)
			for (trial <- 1 to 10) {
				struct.setScale(Vector3DUtil.ONE * scale * 2)
				val extStruct = struct.getExteriorStructure
				extStruct.foreach(v => assertThat(max(abs(v.getX()), abs(v.getY()), abs(v.getZ()))).isEqualTo(scale))
				assertThat(extStruct.size).isEqualTo(2 + (scale * scale * 4) * 6)
				extStructProf.lap()
			}
			println(extStructProf.average)
		}
		/*
		iterateSpace(5, v => {
		if ((v.x == -5 || v.x == 5) && (v.y == -5 || v.y == 5) && (v.z == -5 || v.z == 5)) {
			assertThat(extStruct.contains(v)).isTrue()
		}
		else {
			assertThat(extStruct.contains(v)).isFalse()
		}
		})*/
	}

	@Test
	def testSphere() {
		val struct = new StructureSphere
		struct.stepSize = 0.5
		struct.error = 0.134
		for (scale <- 10 to 500 by 25) {
			val extStructProf = new Profiler("Sphere " + scale)
			for (trial <- 1 to 10) {
				struct.setScale(Vector3DUtil.ONE * scale)
				val extStruct = struct.getExteriorStructure
				extStruct.foreach(v => assertThat(v.getNorm()).isBetween(scale - 1d, scale + 1d))
				assertThat(extStruct.size).isEqualTo(4 * Math.PI * scale * scale)
				extStructProf.lap()
			}
			println(extStructProf.average)
		}
	}

	def iterateSpace(size: Int, f: (Vector3D => Unit)) {
		for (x <- -size to size; z <- -size to size; y <- -size to size) {
			f(new Vector3D(x, y, z))
		}
	}
}

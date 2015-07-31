package com.calclavia.edx.mechanical.physic.grid

import com.calclavia.edx.mechanical.physic.grid.MechanicalGrid._
import org.assertj.core.api.Assertions
import org.junit.Test
import org.assertj.core.api.Assertions._

class MechanicalGridTest {


	@Test
	def testCreation(): Unit = {
		val grid = new MechanicalGrid
		val gear1 = new GearNode(1)
		val gear2 = new GearNode(3)
		grid.add(gear1, Nil)
		grid.add(gear2, (gear1, true) :: Nil)
		grid.recalculate()

		assertThat(gear1.relativeSpeed.get / gear2.relativeSpeed.get).isEqualTo(3)
	}

	@Test
	def testFailingLoop(): Unit = {

		val grid = new MechanicalGrid
		val gear1 = new GearNode(1)
		val gear2 = new GearNode(3)
		val gear3 = new GearNode(2)
		grid.add(gear1, Nil)
		grid.add(gear2, (gear1, false) :: Nil)
		grid.add(gear3, (gear1, true) :: (gear2, true) :: Nil)
		grid.recalculate() match {
			case Loop(x) =>
			case x => throw new AssertionError(s"Loop should have been detected. Result is $x")
		}
	}

	@Test
	def testWorkingLoop(): Unit = {

		val grid = new MechanicalGrid
		val gear1 = new GearNode(1)
		val gear2 = new GearNode(3)
		val gear3 = new GearNode(2)
		grid.add(gear1, Nil)
		grid.add(gear2, (gear1, true) :: Nil)
		grid.add(gear3, (gear1, true) :: (gear2, true) :: Nil)
		grid.recalculate() match {
			case Loop(x) =>
			case x => throw new AssertionError(s"Loop should have been detected. Result is $x")
		}
	}

}

package com.calclavia.graph.thermal

import nova.core.game.Game
import nova.core.util.Direction
import nova.core.util.transform.vector.Vector3i
import nova.core.world.World
import nova.scala.ExtendedUpdater

import scala.collection.mutable

/**
 * A grid managing the flow of thermal energy.
 *
 * Heat flows from hot to cold.
 */

object GridThermal extends ExtendedUpdater {

	private val worldMap = mutable.WeakHashMap.empty[World, GridThermal]

	/**
	 * Gets the thermal grid for this world object. 
	 * @param world
	 */
	def get(world: World): GridThermal = {
		if (!worldMap.contains(world)) {
			val thermal: GridThermal = new GridThermal(world)
			worldMap += (world -> thermal)
			Game.instance.syncTicker.add(thermal)
		}

		return worldMap(world)
	}

	def clear() {
		worldMap.clear()
	}
}

class GridThermal(val world: World) extends ExtendedUpdater {

	/**
	 * A map of positions and heat source energy
	 */
	private var heatMap = Map.empty[Vector3i, Double].withDefaultValue(0d)

	/**
	 * A map of temperature at every block position relative to its default temperature
	 */
	private var deltaTemperatureMap = Map.empty[Vector3i, Int].withDefaultValue(0)

	private var markClear = false

	override def update(deltaTime: Double) {
		heatMap synchronized {
			if (markClear) {
				heatMap = Map.empty
				deltaTemperatureMap = Map.empty
				markClear = false
			}

			//There can't be negative energy, remove all heat values less than zero.
			heatMap --= heatMap.filter(_._2 <= 0).keySet

			heatMap.foreach {
				case (pos, heat) => {
					/**
					 * Heat is used to increase the kinetic energy of blocks, thereby increasing their temperature
					 *
					 * Specific Heat Capacity:
					 * Q = mcT
					 *
					 * Therefore:
					 * T = Q/mc
					 */
					val specificHeatCapacity = 4200 //ThermalPhysics.getSHC(world.getBlock(pos).getMaterial)
					val deltaTemperature = (heat / (1 * specificHeatCapacity)).toInt
					deltaTemperatureMap += pos -> deltaTemperature
				}
			}

			deltaTemperatureMap --= deltaTemperatureMap.filter(_._2 <= 0).keys

			heatMap.foreach {
				case (pos, heat) => {

					/**
					 * Do heat transfer based on thermal conductivity
					 *
					 * Assume transfer by conduction
					 *
					 * Q = k * A * deltaTemp * deltaTime /d
					 * Q = k * deltaTemp * deltaTime
					 *
					 * where k = thermal conductivity, A = 1 m*m, and d = 1 meter (for every block)
					 */
					val temperature = getTemperature(pos)

					Direction.DIRECTIONS
						.map(pos + _.toVector)
						.foreach(
					    adj => {
						    val adjTemp = getTemperature(adj)

						    if (temperature > adjTemp) {
							    //TODO: Based on materials
							    val thermalConductivity = 100 //2.18

							    val heatTransfer = Math.min(thermalConductivity * (temperature - adjTemp) * deltaTime, heat / 6)
							    addHeat(adj, heatTransfer)
							    removeHeat(pos, heatTransfer)
						    }
					    }
						)

				}
			}
		}
	}

	def addHeat(position: Vector3i, heat: Double) {
		heatMap += position -> (heatMap(position) + heat)
	}

	def removeHeat(position: Vector3i, heat: Double) {
		heatMap += position -> (heatMap(position) - heat)
	}

	/**
	 * Gets the temperature at a specific position
	 * @return - Temperature in Kelvin
	 */
	def getTemperature(pos: Vector3i): Int = 295 /*ThermalPhysics.getDefaultTemperature(pos)*/ + deltaTemperatureMap(pos)

	def clear() {
		markClear = true
	}
}
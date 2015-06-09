package com.calclavia.graph.thermal

import net.minecraft.block.material.Material
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * A thermal block manager
 *
 * @author Calclavia
 */
object ThermalPhysics {
	val roomTemperature = 295

	private var materialSHCMap = Map.empty[Material, Int].withDefaultValue(0)

	register(Material.iron, 4500)
	register(Material.air, 1010)
	register(Material.ground, 8000)
	register(Material.wood, 1500)
	register(Material.rock, 8400)
	register(Material.anvil, 5000)
	register(Material.water, 4200)
	register(Material.lava, 9000)
	register(Material.leaves, 8400)
	register(Material.plants, 8400)
	register(Material.vine, 8400)
	register(Material.sponge, 8400)
	register(Material.cloth, 1400)
	register(Material.fire, 1000)
	register(Material.sand, 1381)
	register(Material.circuits, 1000)
	register(Material.carpet, 1130)
	register(Material.glass, 8400)
	register(Material.redstoneLight, 1000)
	register(Material.tnt, 1000)
	register(Material.coral, 8400)
	register(Material.ice, 4200)
	register(Material.snow, 4200)
	register(Material.craftedSnow, 4200)
	register(Material.cactus, 8400)
	register(Material.clay, 1381)
	register(Material.gourd, 8400)
	register(Material.dragonEgg, 8400)
	register(Material.portal, 1000)
	register(Material.cake, 2000)
	register(Material.web, 8400)
	register(Material.piston, 4500)

	/**
	 * Registers a block with a specific heating value
	 * @param sch - The specific heat capacity in J/Kg K
	 */
	def register(mat: Material, sch: Int) {
		materialSHCMap += mat -> sch
	}

	/**
	 * Gets the specific heat capacity of a certain material
	 */
	def getSHC(mat: Material) = materialSHCMap(mat)

	def getTemperatureForEnergy(mass: Float, specificHeatCapacity: Double, energy: Double): Double = {
		return energy / (mass * specificHeatCapacity)
	}

	def getRequiredBoilWaterEnergy(world: World, x: Int, z: Int): Double = {
		return getRequiredBoilWaterEnergy(world, x, z, 1000)
	}

	def getRequiredBoilWaterEnergy(world: World, x: Int, z: Int, volume: Int): Double = {
		val temperatureChange: Float = 373 - ThermalPhysics.getDefaultTemperature(world, new Vector3D(x, 0, z))
		val mass: Float = getMass(volume, 1)
		return ThermalPhysics.getEnergyForTemperatureChange(mass, 4200, temperatureChange) + ThermalPhysics.getEnergyForStateChange(mass, 2257000)
	}

	/**
	 * Temperature: 0.5f = 22C
	 *
	 * @return The temperature of the coordinate in the world in kelvin.
	 */
	def getDefaultTemperature(world: World, vectorWorld: Vector3D): Int = {
		val averageTemperature = 273 + ((world.getBiomeGenForCoords(vectorWorld.getX, vectorWorld.getZ).getFloatTemperature(vectorWorld.getX, 0, vectorWorld.getZ) - 0.4) * 50).toInt
		val dayNightVariance = averageTemperature * 0.05
		return (averageTemperature + (if (world.isDaytime) dayNightVariance else -dayNightVariance)).toInt
	}

	/**
	 * Q = mcT
	 *
	 * @param mass                 - KG
	 * @param specificHeatCapacity - J/KG K
	 * @param temperature          - K
	 * @return Q, energy in joules
	 */
	def getEnergyForTemperatureChange(mass: Double, specificHeatCapacity: Double, temperature: Double): Double = {
		return mass * specificHeatCapacity * temperature
	}

	/**
	 * Q = mL
	 *
	 * @param mass               - KG
	 * @param latentHeatCapacity - J/KG
	 * @return Q, energy in J
	 */
	def getEnergyForStateChange(mass: Float, latentHeatCapacity: Double): Double = {
		return mass * latentHeatCapacity
	}

	/**
	 * Gets the mass of an object from volume and density.
	 *
	 * @param volume  - in liters
	 * @param density - in kg/m^3
	 * @return
	 */
	def getMass(volume: Float, density: Float): Float = {
		return volume / 1000f * density
	}

	/**
	 * Mass (KG) = Volume (Cubic Meters) * Densitry (kg/m-cubed)
	 *
	 * @param fluidStack
	 * @return The mass in KG
	 */
	def getMass(fluidStack: FluidStack): Int = {
		return (fluidStack.amount / 1000) * fluidStack.getFluid.getDensity(fluidStack)
	}

	/**
	 * Default handler.

	@SubscribeEvent
	def thermalEventHandler(evt: ThermalEvent.EventThermalUpdate) {
		val pos = evt.position
		val block = pos.getBlock

		if (block == Blocks.flowing_water || block == Blocks.water) {
			if (evt.temperature >= 373) {
				if (FluidRegistry.getFluid("steam") != null) {
					val volume = FluidContainerRegistry.BUCKET_VOLUME * (evt.temperature / 373)
					MinecraftForge.EVENT_BUS.post(new BoilEvent(pos.world, pos, new FluidStack(FluidRegistry.WATER, volume), new FluidStack(FluidRegistry.getFluid("steam"), volume), 2))
				}
			}
		}

		if (block == Blocks.ice) {
			if (evt.temperature >= 273) {
				UpdateTicker.threaded.enqueue(() => pos.setBlock(Blocks.flowing_water))
			}
		}
	}*/
}